/**
 * @description:
 * @author: 29874
 * @date: 2025/11/14 23:20
 */

package com.yue.api;

import com.yue.api.dto.SeckillVoucherRequestDTO;
import com.yue.api.response.Response;

public interface ISeckillVoucherService {
    /**
     * 添加秒杀优惠券
     * @param requestDTO
     * @return
     */
    Response<Void> addSeckillVoucher(SeckillVoucherRequestDTO requestDTO);
}
