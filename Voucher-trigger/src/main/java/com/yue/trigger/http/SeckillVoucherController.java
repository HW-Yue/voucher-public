/**
 * @description:
 * @author: 29874
 * @date: 2025/11/14 23:22
 */

package com.yue.trigger.http;

import com.yue.api.ISeckillVoucherService;
import com.yue.api.dto.SeckillVoucherRequestDTO;
import com.yue.api.response.Response;
import com.yue.domain.voucher.model.entity.SeckillVoucherEntity;
import com.yue.domain.voucher.service.IVoucherService;
import com.yue.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/voucher")
public class SeckillVoucherController implements ISeckillVoucherService {
    @Resource
    private IVoucherService voucherService;

    @PostMapping("/seckill")
    @Override
    public Response<Void> addSeckillVoucher(@RequestBody SeckillVoucherRequestDTO requestDTO) {
        try{

        //添加秒杀优惠卷
        voucherService.addSeckillVoucher(
                SeckillVoucherEntity.builder()
                        .shopId(requestDTO.getShopId())
                        .title(requestDTO.getTitle())
                        .subTitle(requestDTO.getSubTitle())
                        .rules(requestDTO.getRules())
                        .payValue(requestDTO.getPayValue())
                        .actualValue(requestDTO.getActualValue())
                        .type(requestDTO.getType())
                        .stock(requestDTO.getStock())
                        .status(1)
                        .beginTime(requestDTO.getBeginTime())
                        .endTime(requestDTO.getEndTime())
                        .build()
        );
        return Response.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .build();
        } catch (Exception e) {
            log.error("添加秒杀优惠券失败", e);
            return Response.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
