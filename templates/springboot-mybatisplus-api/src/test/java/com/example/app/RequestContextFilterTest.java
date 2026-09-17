package com.example.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.app.common.RequestContextFilter;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * 请求上下文过滤器生命周期测试：响应提交前必须已有 request-id，
 * 异常请求也必须留下访问日志并清理 MDC。
 */
class RequestContextFilterTest {

    private final RequestContextFilter filter = new RequestContextFilter();

    @Test
    void requestIdExistsBeforeResponseIsCommitted() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/stream");
        request.addHeader("X-Request-ID", "trace-committed");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            MockHttpServletResponse currentResponse = (MockHttpServletResponse) servletResponse;
            assertEquals("trace-committed", currentResponse.getHeader("X-Request-ID"));
            currentResponse.flushBuffer();
        });

        assertTrue(response.isCommitted());
        assertEquals("trace-committed", response.getHeader("X-Request-ID"));
        assertNull(MDC.get("requestId"));
    }

    @Test
    void failureIsLoggedAndMdcIsCleared() {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestContextFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/failure");
            request.addHeader("X-Request-ID", "trace-failure");
            MockHttpServletResponse response = new MockHttpServletResponse();

            assertThrows(ServletException.class, () -> filter.doFilter(request, response,
                    (servletRequest, servletResponse) -> {
                        throw new ServletException("内部失败");
                    }));

            assertEquals("trace-failure", response.getHeader("X-Request-ID"));
            assertNull(MDC.get("requestId"));
            assertTrue(appender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .anyMatch(message -> message.contains("request_id=trace-failure")
                            && message.contains("failed=true")));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

}
