/**
 * @description:
 * @author: 29874
 * @date: 2025/11/15 11:46
 */

package com.yue.infrastructure.dao;

import com.yue.infrastructure.dao.po.SeckillVoucher;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ISeckillVoucherDao {
    void addSeckillVoucher(SeckillVoucher seckillVoucher);

    void updateStock(Long voucherId);
}
