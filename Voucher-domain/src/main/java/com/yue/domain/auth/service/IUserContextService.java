/**
 * @description:
 * @author: 29874
 * @date: 2025/11/16 21:56
 */

package com.yue.domain.auth.service;

public interface IUserContextService {

    /**
     * 获取当前认证用户的role。
     * @throws RuntimeException 如果用户未认证
     */
    String getCurrentRole();
    /**
     * 获取当前认证用户的id。
     * @throws RuntimeException 如果用户未认证
     */
    Long getCurrentUserId();

}
