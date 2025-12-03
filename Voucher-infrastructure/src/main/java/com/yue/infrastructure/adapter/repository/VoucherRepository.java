/**
 * @description:
 * @author: 29874
 * @date: 2025/11/14 22:55
 */

package com.yue.infrastructure.adapter.repository;

import com.yue.domain.voucher.adapter.repository.IVoucherRepository;
import com.yue.domain.voucher.model.entity.SeckillVoucherEntity;
import com.yue.domain.voucher.model.entity.VoucherOrderEntity;
import com.yue.infrastructure.dao.ISeckillVoucherDao;
import com.yue.infrastructure.dao.IVoucherDao;
import com.yue.infrastructure.dao.IVoucherOrderDao;
import com.yue.infrastructure.dao.po.SeckillVoucher;
import com.yue.infrastructure.dao.po.Voucher;
import com.yue.infrastructure.dao.po.VoucherOrder;
import com.yue.types.enums.OrderStatus;
import com.yue.types.enums.ResponseCode;
import com.yue.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Repository
public class VoucherRepository implements IVoucherRepository {
    @Resource
    private IVoucherDao voucherDao;

    @Resource
    private ISeckillVoucherDao seckillVoucherDao;

    @Resource
    private IVoucherOrderDao voucherOrderDao;
    /**
     * 添加秒杀券
     * @param seckillVoucherEntity
     */
    @Override
    public SeckillVoucherEntity addSeckillVoucher(SeckillVoucherEntity seckillVoucherEntity) {

        //使用voucher构建TbVoucher
        Voucher voucher = Voucher.builder()
                .shopId(seckillVoucherEntity.getShopId())
                .title(seckillVoucherEntity.getTitle())
                .subTitle(seckillVoucherEntity.getSubTitle())
                .rules(seckillVoucherEntity.getRules())
                .payValue(seckillVoucherEntity.getPayValue())
                .actualValue(seckillVoucherEntity.getActualValue())
                .type(seckillVoucherEntity.getType())
                .status(seckillVoucherEntity.getStatus())
                .build();
        voucherDao.addVoucher(voucher);
        Long id=voucher.getId();
        //使用Voucher构建TbSeckillVoucher
        SeckillVoucher seckillVoucher = SeckillVoucher.builder()
                .voucherId(id)
                .stock(seckillVoucherEntity.getStock())
                .beginTime(seckillVoucherEntity.getBeginTime())
                .endTime(seckillVoucherEntity.getEndTime())
                .build();

        try {
            seckillVoucherDao.addSeckillVoucher(seckillVoucher);
        }catch (DuplicateKeyException e){
            throw new AppException(ResponseCode.INDEX_EXCEPTION);
        }
        return SeckillVoucherEntity.builder()
                .id(Long.valueOf(id))
                .shopId(seckillVoucherEntity.getShopId())
                .title(seckillVoucherEntity.getTitle())
                .subTitle(seckillVoucherEntity.getSubTitle())
                .rules(seckillVoucherEntity.getRules())
                .payValue(seckillVoucherEntity.getPayValue())
                .actualValue(seckillVoucherEntity.getActualValue())
                .type(seckillVoucherEntity.getType())
                .status(seckillVoucherEntity.getStatus())
                .stock(seckillVoucherEntity.getStock())
                .beginTime(seckillVoucherEntity.getBeginTime())
                .endTime(seckillVoucherEntity.getEndTime())
                .build();
    }

    @Transactional
    @Override
    public void addSeckillOrder(VoucherOrderEntity voucherOrderEntity) {
        //1.将tb_seckill_voucher的voucherId的stock库存减1
        //seckillVoucherDao.updateStock(voucherOrderEntity.getVoucherId());
        //2.将订单写入tb_voucher_order
        VoucherOrder voucherOrder = VoucherOrder.builder()
                .userId(voucherOrderEntity.getUserId())
                .voucherId(voucherOrderEntity.getVoucherId())
                .orderId(voucherOrderEntity.getOrderId())
                .status(OrderStatus.UNPAID.getCode())
                .build();
        try{
        voucherOrderDao.addVoucherOrder(voucherOrder);
        }catch (Exception e){
            log.error("订单写入数据库失败",e);
        }

    }
}
