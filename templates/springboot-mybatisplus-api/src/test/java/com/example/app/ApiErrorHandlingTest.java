package com.example.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.app.common.GlobalExceptionHandler;
import com.example.app.common.RequestContextFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 错误信封与脱敏测试：错误体统一为 ApiResponse，且不回显内部细节。
 * 使用独立的测试 controller 触发各类错误，不影响业务代码。
 */
class ApiErrorHandlingTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new FailingController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .addFilters(new RequestContextFilter())
            .build();

    @Test
    void validationErrorIsSanitizedAndEnveloped() throws Exception {
        mockMvc.perform(post("/_test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": -1}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(422))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.data.errors[0].field").value("value"))
                .andExpect(jsonPath("$.data.errors[0].input").doesNotExist());
    }

    @Test
    void malformedJsonReturnsBadRequestEnvelope() throws Exception {
        mockMvc.perform(post("/_test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"));
    }

    @Test
    void unsupportedMediaTypeReturns415Envelope() throws Exception {
        mockMvc.perform(post("/_test/validation")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("value=-1"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value(415))
                .andExpect(jsonPath("$.message").value("Unsupported Media Type"));
    }

    @Test
    void unsupportedMethodReturns405Envelope() throws Exception {
        mockMvc.perform(post("/_test/unhandled"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value(405))
                .andExpect(jsonPath("$.message").value("Method Not Allowed"));
    }

    @Test
    void responseStatusExceptionPreservesStatusWithoutLeakingReason() throws Exception {
        mockMvc.perform(get("/_test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("Conflict"));
    }

    @Test
    void unhandledErrorIsEnvelopedWithoutInternalDetails() throws Exception {
        mockMvc.perform(get("/_test/unhandled"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Internal Server Error"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    private record SampleRequest(@Min(value = 0, message = "必须为非负数") int value) {
    }

    @RestController
    static class FailingController {

        @PostMapping("/_test/validation")
        public SampleRequest validation(@Valid @RequestBody SampleRequest request) {
            return request;
        }

        @GetMapping("/_test/conflict")
        public Object conflict() {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "内部冲突细节");
        }

        @GetMapping("/_test/unhandled")
        public Object unhandled() {
            throw new IllegalStateException("数据库密码不应泄露");
        }

    }

}
