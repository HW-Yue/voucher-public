/**
 * @description:
 * @author: 29874
 * @date: 2025/11/14 22:56
 */

package com.yue.domain.voucher.adapter.repository;

import com.yue.domain.voucher.model.entity.SeckillVoucherEntity;
import com.yue.domain.voucher.model.entity.VoucherOrderEntity;

public interface IVoucherRepository {

    SeckillVoucherEntity addSeckillVoucher(SeckillVoucherEntity voucher);

    void addSeckillOrder(VoucherOrderEntity voucherOrderEntity);
}
