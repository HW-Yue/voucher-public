/**
 * @description:
 * @author: 29874
 * @date: 2025/11/14 22:43
 */

package com.yue.domain.voucher.service;

import com.yue.domain.voucher.model.entity.SeckillVoucherEntity;


public interface IVoucherService {
    /**
     * 添加秒杀券
     * @param voucher 秒杀券
     */
    void addSeckillVoucher(SeckillVoucherEntity voucher);
}
