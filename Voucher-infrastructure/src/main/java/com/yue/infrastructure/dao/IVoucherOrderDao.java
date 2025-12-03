/**
 * @description:
 * @author: 29874
 * @date: 2025/11/17 10:26
 */

package com.yue.infrastructure.dao;

import com.yue.infrastructure.dao.po.VoucherOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IVoucherOrderDao {

    void addVoucherOrder(VoucherOrder Order);
}
