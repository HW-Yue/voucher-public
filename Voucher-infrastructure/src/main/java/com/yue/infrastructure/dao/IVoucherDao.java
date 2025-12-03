/**
 * @description: 
 * @author: 29874
 * @date: 2025/11/14 22:52
 */

package com.yue.infrastructure.dao;

import com.yue.infrastructure.dao.po.Voucher;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IVoucherDao {


    int addVoucher(Voucher voucher);
}
