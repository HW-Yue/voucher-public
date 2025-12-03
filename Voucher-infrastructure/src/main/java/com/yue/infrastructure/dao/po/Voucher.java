/**
 * @description:
 * @author: 29874
 * @date: 2025/11/14 22:49
 */

package com.yue.infrastructure.dao.po;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 对应数据库表 tb_voucher
 * 优惠券基本信息
 */
@Builder
@Data // 自动生成 Getter, Setter, toString, equals, hashCode
@NoArgsConstructor // 自动生成无参构造函数
@AllArgsConstructor // 自动生成全参构造函数
public class Voucher {

    /**
     * 主键
     */
    private Long id;

    /**
     * 商铺id
     */
    private Long shopId;

    /**
     * 代金券标题
     */
    private String title;

    /**
     * 副标题
     */
    private String subTitle;

    /**
     * 使用规则
     */
    private String rules;

    /**
     * 支付金额，单位是分。例如200代表2元
     */
    private Long payValue;

    /**
     * 抵扣金额，单位是分。例如200代表2元
     */
    private Long actualValue;

    /**
     * 类型：0-普通券；1-秒杀券
     */
    private Integer type;

    /**
     * 状态：1-上架; 2-下架; 3-过期
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;



    @Override
    public String toString() {
        return "TbVoucher{" +
                "id=" + id +
                ", shopId=" + shopId +
                ", title='" + title + '\'' +
                ", subTitle='" + subTitle + '\'' +
                ", rules='" + rules + '\'' +
                ", payValue=" + payValue +
                ", actualValue=" + actualValue +
                ", type=" + type +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}