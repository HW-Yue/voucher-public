/**
 * @description:
 * @author: 29874
 * @date: 2025/11/15 17:48
 */

package com.yue.domain.voucher.adapter.port;

public interface IdGenerator {
    /**
     * 根据业务前缀生成下一个唯一ID
     */
    long nextId(String keyPrefix);
}
