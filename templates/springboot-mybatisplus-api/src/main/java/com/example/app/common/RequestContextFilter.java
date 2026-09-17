package com.example.app.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 请求上下文：校验或生成 X-Request-ID、写入 MDC 供日志模式输出、
 * 回写响应头并记录访问日志。访问日志只记录请求 ID、方法、路径、状态和耗时，
 * 不记录 query string、body、Authorization、Cookie、token 或密码。
 */
@Component("appRequestContextFilter")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestContextFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestContextFilter.class);
    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("[A-Za-z0-9._-]{1,128}");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || !REQUEST_ID_PATTERN.matcher(requestId).matches()) {
            requestId = UUID.randomUUID().toString().replace("-", "");
        }

        response.setHeader("X-Request-ID", requestId);
        MDC.put("requestId", requestId);
        long startedAt = System.nanoTime();
        Throwable failure = null;
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException | Error exception) {
            failure = exception;
            throw exception;
        } finally {
            log.info("请求完成 request_id={} method={} path={} status={} duration_ms={} failed={}",
                    requestId, request.getMethod(), request.getRequestURI(),
                    response.getStatus(),
                    Duration.ofNanos(System.nanoTime() - startedAt).toMillis(),
                    failure != null);
            MDC.remove("requestId");
        }
    }

}
