package com.yue.config.security;

import com.alibaba.fastjson.JSON;
import com.yue.trigger.http.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class AppAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        log.error("access error", accessDeniedException);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        response.getWriter().println(JSON.toJSONString(Response.<String>builder()
                .code(Response.ResponseCode.DENIED.getCode())
                .info(Response.ResponseCode.DENIED.getInfo())
                .build()));

        response.getWriter().flush();
    }

}
