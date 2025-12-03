/**
 * @description:
 * @author: 29874
 * @date: 2025/11/16 21:56
 */

package com.yue.domain.auth.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserContextServiceImpl implements IUserContextService{
    @Override
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录或Token验证失败");
        }

        Object principal = authentication.getPrincipal();

        // ⚡ 关键修改：直接将 Principal 转换为 Long
        if (principal instanceof Long) {
            return (Long) principal;
        }

        // 如果是 String，尝试转换为 Long (兼容性处理，但最好直接用 Long)
        if (principal instanceof String) {
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                // 无法转换，抛出错误
            }
        }

        throw new RuntimeException("无法获取用户ID，Principal类型错误。");
    }

    @Override
    public String getCurrentRole() {
        return "";
    }


}
