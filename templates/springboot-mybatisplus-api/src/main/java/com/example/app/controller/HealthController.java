package com.example.app.controller;

import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 探针端点：/health 是纯 liveness，不访问数据库；
 * /ready 只检查数据库连通性，不迁移、建表或执行业务查询。
 * 探针不使用 ApiResponse 包装。
 */
@RestController
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "up");
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> ready() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        } catch (DataAccessException exception) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("status", "unavailable"));
        }
        return ResponseEntity.ok(Map.of("status", "up"));
    }

}
