/**
 * @description:
 * @author: 29874
 * @date: 2025/11/14 23:14
 */

package com.yue.api.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 新增秒杀优惠券的请求DTO
 * 包含 tb_voucher 和 tb_seckill_voucher 的字段
 */
@Data
public class SeckillVoucherRequestDTO {

    /**
     * 商铺ID (必填)
     */
    @NotNull(message = "商铺ID不能为空")
    @Positive(message = "商铺ID必须是正数")
    private Long shopId;

    /**
     * 优惠券标题 (必填)
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 副标题 (非必填)
     */
    private String subTitle;

    /**
     * 使用规则 (非必填)
     */
    private String rules;

    /**
     * 支付金额 (分) (必填)
     */
    @NotNull(message = "支付金额不能为空")
    @Min(value = 0, message = "支付金额不能为负")
    private Long payValue;

    /**
     * 抵扣金额 (分) (必填)
     */
    @NotNull(message = "抵扣金额不能为空")
    @Positive(message = "抵扣金额必须大于零")
    private Long actualValue;

    /**
     * 优惠券类型：1 (秒杀券) (必填)
     */
    @NotNull(message = "优惠券类型不能为空")
    @Min(value = 1, message = "优惠券类型数值无效")
    private Integer type;

    // --- 秒杀特有字段 ---

    /**
     * 库存 (秒杀券必填)
     */
    @NotNull(message = "库存不能为空")
    @Min(value = 1, message = "库存必须大于0")
    private Integer stock;

    /**
     * 生效时间 (秒杀开始时间) (必填)
     * 注意：使用 LocalDateTime 类型，Spring Boot会自动处理 "yyyy-MM-ddTHH:mm:ss" 格式的JSON字符串。
     */
    @NotNull(message = "生效时间不能为空")
    private LocalDateTime beginTime;

    /**
     * 失效时间 (秒杀结束时间) (必填)
     */
    @NotNull(message = "失效时间不能为空")
    private LocalDateTime endTime;
}