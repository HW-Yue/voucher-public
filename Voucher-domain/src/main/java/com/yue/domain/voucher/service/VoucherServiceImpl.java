/**
 * @description:
 * @author: 29874
 * @date: 2025/11/14 22:46
 */

package com.yue.domain.voucher.service;

import com.yue.domain.voucher.adapter.repository.IVoucherRepository;
import com.yue.domain.voucher.model.entity.SeckillVoucherEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.yue.types.common.RedisConstants.SECKILL_STOCK_KEY;

@Service
@Slf4j
public class VoucherServiceImpl implements IVoucherService {

    @Resource
    private IVoucherRepository repository;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public void addSeckillVoucher(SeckillVoucherEntity seckillVoucherEntity) {
        log.info("为店铺添加秒杀优惠卷,shopId:{} title:{}", seckillVoucherEntity.getShopId(), seckillVoucherEntity.getTitle());
        SeckillVoucherEntity seckillVoucher= repository.addSeckillVoucher(seckillVoucherEntity);
        try {
            // 保存秒杀库存到Redis中
            stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + seckillVoucher.getId(), seckillVoucher.getStock().toString());
        } catch (Exception e) {
            log.error("保存秒杀库存到redis失败", e);
        }
    }
}
