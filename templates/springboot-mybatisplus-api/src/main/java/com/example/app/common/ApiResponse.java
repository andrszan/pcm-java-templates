package com.example.app.common;

/**
 * 业务响应统一包装：code 与 HTTP 状态码一致，data 缺省为 null。
 * 探针端点（/health、/ready）不使用本包装。
 */
public record ApiResponse<T>(int code, String message, T data) {

    public static <T> ApiResponse<T> of(int code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }

}
