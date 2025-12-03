package com.yue.config.security;

import cn.hutool.jwt.JWTUtil;
import com.yue.types.enums.RoleTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final static String AUTH_HEADER = "Authorization";
    private final static String AUTH_HEADER_TYPE = "Bearer";



    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTH_HEADER);

        // 1. 检查 Header 是否存在
        if (Objects.isNull(authHeader) || !authHeader.startsWith(AUTH_HEADER_TYPE)){
            filterChain.doFilter(request,response);
            log.error("不存在Header");
            return;
        }

        String authToken = authHeader.split(" ")[1];

        // 💫 关键修改：将所有 JWT 操作都放入 try 块中
        try {
            // 2. 解析 Token (Hutool 推荐先解析)
            cn.hutool.jwt.JWT jwt = JWTUtil.parseToken(authToken);

            // 3. 验证 Token (使用实例验证)
            // 这一步会同时检查签名和有效期 (exp)，如果失败会抛出异常
            boolean isValid = jwt.setKey("key".getBytes(StandardCharsets.UTF_8)).verify();

            if (!isValid) {
                // 签名不正确
                throw new RuntimeException("JWT validation failed: Invalid signature");
            }

            // 1. 先将其作为 Number (数字) 类型获取，这是 Integer 和 Long 的父类
            Number userIdAsNumber = (Number) jwt.getPayload("userId");

            // 2. 调用 .longValue() 方法安全地转换为 Long
            final Long userId = userIdAsNumber.longValue();

            // 5. 提取权限
            Object rolesClaim = jwt.getPayload("roles");


            Collection<SimpleGrantedAuthority> authorities;

            if (rolesClaim instanceof List) {
                // 💫 关键修改：更安全的类型转换
                List<?> rolesList = (List<?>) rolesClaim;
                authorities = rolesList.stream()
                        .map(obj -> (Integer) obj) // 显式转换每个元素
                        .map(roleCode -> {
                            // 依赖 RoleTypeEnum.java 中的静态方法
                            String roleName = RoleTypeEnum.getNameByCode(roleCode);
                            // 转换为 "ROLE_ADMIN", "ROLE_USER"
                            return new SimpleGrantedAuthority("ROLE_" + roleName);
                        })
                        .collect(Collectors.toList());
            } else {
                authorities = Collections.emptyList();
            }

            // 6. 构建 Authentication
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 7. 放入 SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // 捕获所有异常 (TokenExpiredException, SignatureVerificationException, etc.)
            log.error("JWT Token processing failed", e);
        }

        // 8. 无论成功还是失败，都继续链
        // 如果验证成功，SecurityContext 中就有 Authentication
        // 如果验证失败 (catch)，SecurityContext 为空，后续的 Spring Security 过滤器会
        // 捕获到 "未认证"，并触发 AppUnauthorizedHandler 返回 401
        filterChain.doFilter(request, response);
    }

}
