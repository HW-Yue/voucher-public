/**
 * @description:
 * @author: 29874
 * @date: 2025/11/15 15:04
 */

package com.yue.api;

import com.yue.api.response.Response;

public interface ISeckillOrderService {
    /**
     * 秒杀下单
     * @param voucherId
     * @return
     */
    Response<Void> seckillOrder(Long voucherId);

}
