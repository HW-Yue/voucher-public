/**
 * @description:
 * @author: 29874
 * @date: 2025/11/17 11:09
 */

package com.yue.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum OrderStatus {
    //订单状态，1：未支付；2：已支付；3：已核销；4：已取消；5：退款中；6：已退款
    UNPAID(0, "未支付"),
    PAID(1, "已支付"),
    VERIFIED(2, "已核销"),
    CANCELED(3, "已取消"),
    REFUNDING(4, "退款中"),
    REFUNDED(5, "已退款"),

        ;
    private int code;
    private String info;


}
