/**
 * @description:
 * @author: 29874
 * @date: 2025/11/15 14:49
 */

package com.yue.trigger.http;

import com.yue.api.ISeckillOrderService;
import com.yue.api.response.Response;
import com.yue.domain.auth.service.IUserContextService;
import com.yue.domain.voucher.service.IVoucherOrderService;
import com.yue.types.enums.ResponseCode;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/voucher-order")
public class SeckillOrderController implements ISeckillOrderService {

    @Resource
    private IVoucherOrderService voucherOrderService;
    @ Resource
    private IUserContextService userContextService;
    @Override
    @RequestMapping("/seckill/{id}")
    public Response<Void>  seckillOrder(@PathVariable("id") Long id) {
        try {
            Long userId=userContextService.getCurrentUserId();
            voucherOrderService.seckillOrder(id,userId);

            return Response.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (Exception e) {
            return Response.<Void>builder()
                    .code("500").info("下单失败")
                    .build();
        }
    }

}
