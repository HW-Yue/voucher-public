/**
 * @description:
 * @author: 29874
 * @date: 2025/11/15 15:02
 */

package com.yue.domain.voucher.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import com.yue.domain.voucher.adapter.port.IdGenerator;
import com.yue.domain.voucher.adapter.repository.IVoucherRepository;
import com.yue.domain.voucher.model.entity.SeckillVoucherEntity;
import com.yue.domain.voucher.model.entity.VoucherOrderEntity;
import com.yue.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



@Slf4j
@Service
public class VoucherOrderServiceImpl implements IVoucherOrderService, DisposableBean {
    @Resource
    private RedissonClient redissonClient;
    // 2. 添加运行标志位
    private volatile boolean isRunning = true;

    @Resource
    private IdGenerator redisIdWorker;

    @Resource
    private IVoucherRepository repository;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    // 3. 在 Spring 销毁时，执行优雅关闭逻辑
    @Override
    public void destroy() throws Exception {
        log.warn("Spring 容器关闭，正在优雅停止订单处理线程...");
        this.isRunning = false; // 设置停止标志，让 while 循环退出

        // 强制关闭线程池，防止它占用资源
        SECKILL_ORDER_EXECUTOR.shutdownNow();
    }


    //SECKILL_SCRIPT： 定义了一个用于执行 Lua 脚本的对象。
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    //static { ... } (静态初始化块)： 这段代码会在该类被加载到 JVM 时立即执行，且只执行一次。
    //
    //SECKILL_SCRIPT： 定义了一个用于执行 Lua 脚本的对象。
    //
    //new ClassPathResource("seckill.lua")： 加载 Lua 脚本文件。 这会将名为 seckill.lua 的脚本文件从项目的类路径（通常是 resources 目录）中读取出来。
    //
    //setResultType(Long.class)： 设置 Lua 脚本执行后的返回值类型为 Long。在秒杀业务中，Lua 脚本通常返回 0 (成功) 或非零值 (错误码)。
    //SECKILL_SCRIPT： 定义了一个用于执行 Lua 脚本的对象。
    //
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("redis/seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    //创建了一个线程池 (ExecutorService)。
    //创建了一个单线程执行器。这意味着所有的订单处理任务都将由这唯一的一个后台线程按顺序执行。
    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    @PostConstruct
    private void init(){
        //将 VoucherOrderHandler（您的后台工作线程/消费者任务）提交给线程池并立即启动。
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }
    private class VoucherOrderHandler implements Runnable{
        private final String queueName = "stream.orders";
        @Override
        public void run() {
            while (isRunning) {
                try {
                    // 0.初始化stream
                    initStream();
                    // 1.获取消息队列中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS s1 >
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(queueName, ReadOffset.lastConsumed())
                    );
                    // 2.判断订单信息是否为空
                    if (list == null || list.isEmpty()) {
                        // 如果为null，说明没有消息，继续下一次循环
                        continue;
                    }
                    // 解析数据
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> value = record.getValue();
                    VoucherOrderEntity voucherOrderEntity = BeanUtil.fillBeanWithMap(value, new VoucherOrderEntity(), true);
                    // 🚀 手动解析并赋值关键的 Long 类型字段
                    // 1. 获取 Redis Stream 中的 String 值
                    Object rawUserId = value.get("userId");
                    Object rawVoucherId = value.get("voucherId");
                    Object rawOrderId = value.get("orderId");

                    // 2. 将 String 显式转换为 Long，并赋值给实体
                    if (rawUserId != null) {
                        // 假设您在实体中使用的字段名是 userId
                        voucherOrderEntity.setUserId(Long.valueOf(rawUserId.toString()));
                    }
                    if (rawVoucherId != null) {
                        voucherOrderEntity.setVoucherId(Long.valueOf(rawVoucherId.toString()));
                    }
                    if (rawOrderId != null) {
                        // 假设您在实体中使用的字段名是 id
                        voucherOrderEntity.setOrderId(Long.valueOf(rawOrderId.toString()));
                    }
                    // 3.创建订单
                    handleVoucherOrder(voucherOrderEntity);
                    // 4.确认消息 XACK stream.orders g1 id
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
                } catch (Exception e) {
                    log.error("处理订单异常", e);
                    handlePendingList();
                }
            }
        }

        public void initStream(){
            Boolean exists = stringRedisTemplate.hasKey(queueName);
            if (BooleanUtil.isFalse(exists)) {
                log.info("stream不存在，开始创建stream");
                // 不存在，需要创建
                stringRedisTemplate.opsForStream().createGroup(queueName, ReadOffset.latest(), "g1");
                log.info("stream和group创建完毕");
                return;
            }
            // stream存在，判断group是否存在
            StreamInfo.XInfoGroups groups = stringRedisTemplate.opsForStream().groups(queueName);
            if(groups.isEmpty()){
                log.info("group不存在，开始创建group");
                // group不存在，创建group
                stringRedisTemplate.opsForStream().createGroup(queueName, ReadOffset.latest(), "g1");
                log.info("group创建完毕");
            }
        }

        private void handlePendingList() {
            while (isRunning) {
                try {
                    // 检查连接状态
                    if (stringRedisTemplate.getConnectionFactory() != null) {
                        ((LettuceConnectionFactory) stringRedisTemplate.getConnectionFactory()).validateConnection();
                    }
                    // 1.获取消息队列中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 STREAMS s1 0
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1),
                            StreamOffset.create(queueName, ReadOffset.from("0"))
                    );
                    // 2.判断订单信息是否为空
                    if (list == null || list.isEmpty()) {
                        // 如果为null，说明没有消息，继续下一次循环
                        break;
                    }
                    // 解析数据
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> value = record.getValue();
                    VoucherOrderEntity voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrderEntity(), true);
                    // 🚀 手动解析并赋值关键的 Long 类型字段
                    // 1. 获取 Redis Stream 中的 String 值
                    Object rawUserId = value.get("userId");
                    Object rawVoucherId = value.get("voucherId");
                    Object rawId = value.get("id");

                    // 2. 将 String 显式转换为 Long，并赋值给实体
                    if (rawUserId != null) {
                        // 假设您在实体中使用的字段名是 userId
                        voucherOrder.setUserId(Long.valueOf(rawUserId.toString()));
                    }
                    if (rawVoucherId != null) {
                        voucherOrder.setVoucherId(Long.valueOf(rawVoucherId.toString()));
                    }
                    if (rawId != null) {
                        // 假设您在实体中使用的字段名是 id
                        voucherOrder.setId(Long.valueOf(rawId.toString()));
                    }
                    // 3.创建订单
                    handleVoucherOrder(voucherOrder);
                    // 4.确认消息 XACK stream.orders g1 id
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
                } catch (Exception e) {
                    log.error("处理订单异常", e);
                }
            }
        }
    }
    private void handleVoucherOrder(VoucherOrderEntity voucherOrderEntity) {
        Long userId = voucherOrderEntity.getUserId();
        // 创建锁对象
        // SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
        RLock lock = redissonClient.getLock("lock:order:" + userId);
        // 获取锁
        boolean isLock = lock.tryLock();
        // 判断是否获取锁成功
        if(!isLock){
            // 获取锁失败，返回错误或重试
            log.error("不允许重复下单");
            return;
        }
        try {
            repository.addSeckillOrder(voucherOrderEntity);
            log.info("创建订单写入数据库持久化");
        } finally {
            // 释放锁
            lock.unlock();
        }
    }
    @Override
    public void seckillOrder(Long voucherId,Long userId) {

        long orderId = redisIdWorker.nextId("order");
        try {
            // 1.执行lua脚本
            Long result = stringRedisTemplate.execute(
                    SECKILL_SCRIPT,
                    Collections.emptyList(),
                    voucherId.toString(), userId.toString(), String.valueOf(orderId)
            );
            int r = result.intValue();
            // 2.判断结果是否为0
            if (r != 0) {
                // 2.1.不为0 ，代表没有购买资格
                log.error("没有购买资格");
            }
            // 4.返回订单id
            log.info("订单创建成功，订单id：{}", orderId);
        } catch (Exception e) {
            log.error("redis扣减库存失败" , e);
        }
    }


}
