package com.yue.config;

import com.yue.config.security.AppAccessDeniedHandler;
import com.yue.config.security.AppUnauthorizedHandler;
import com.yue.config.security.JwtAuthenticationTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SpringSecurityConfig {

    // 不拦截的 URL
    private final String[] requestMatchers = {"/api/auth/login", "/api/auth/register", "/api/auth/query_user_name", "/test/**"};

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter() {
        return new JwtAuthenticationTokenFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity,
                                           JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter,
                                           AppUnauthorizedHandler appUnauthorizedHandler,
                                           AppAccessDeniedHandler appAccessDeniedHandler
    ) throws Exception {
        // 使用JWT，可屏蔽csrf防护
        httpSecurity.csrf(CsrfConfigurer::disable)
                // 基于token存储到浏览器，不需要session
                .sessionManagement(sessionManagementConfigurer -> sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeRequests(authorizationRegistry -> authorizationRegistry
                        // 允许对于网站静态资源的无授权访问
                        .antMatchers(HttpMethod.GET, "/", "/*.html").permitAll()
                        // 对登录注册允许匿名访问
                        .antMatchers(requestMatchers).permitAll()
                        // 只需要认证
                        .antMatchers("/voucher-order/**").authenticated()
                        // 需要 ADMIN 角色
                        .antMatchers("/voucher/**").hasRole("ADMIN")
                        // 跨域请求会先进行一次options请求
                        .antMatchers(HttpMethod.OPTIONS).permitAll()
                        // 对所有请求开启授权保护
                        .anyRequest()
                        // 已认证的请求自动被授权
                        .authenticated()
                )
                // 禁用缓存
                .headers(headersConfigurer -> headersConfigurer
                        .cacheControl(HeadersConfigurer.CacheControlConfig::disable)
                )
                // 添加 JWT filter
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                // 添加自定义未授权和未登录结果返回
                .exceptionHandling(exceptionConfigure -> exceptionConfigure
                        .accessDeniedHandler(appAccessDeniedHandler)
                        .authenticationEntryPoint(appUnauthorizedHandler));

        return httpSecurity.build();
    }

}
