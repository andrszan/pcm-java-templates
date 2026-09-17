package com.example.app;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * 数据库集成测试：验证数据源、MyBatis-Plus 和 Flyway 迁移链路真实可用。
 * 测试始终连接 TEST_DB_NAME 指定的测试库，不影响开发库。
 * 未配置 TEST_DB_NAME 时跳过（与 Python 模板的缺库跳过模式一致）。
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "TEST_DB_NAME", matches = ".+",
        disabledReason = "未配置 TEST_DB_NAME 时跳过需要真实数据库的集成测试")
class ApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void useTestDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> protocol() + "://" + host() + ":" + port() + "/" + required("TEST_DB_NAME"));
    }

    @Test
    void contextLoadsWithDatabase() {
        // 连接池是惰性的，必须真实执行一次 SQL 才能证明数据源可用
        Assertions.assertEquals(1, jdbcTemplate.queryForObject("SELECT 1", Integer.class));
    }

    private static String protocol() {
        return env("DB_PROTOCOL", "jdbc:mysql");
    }

    private static String host() {
        return env("DB_HOST", "127.0.0.1");
    }

    private static String port() {
        return env("DB_PORT", "3306");
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String required(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("缺少必需环境变量 " + key);
        }
        return value;
    }

}
