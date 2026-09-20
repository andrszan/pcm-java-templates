package com.example.app;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.StringUtils;

/**
 * 数据库集成测试：验证数据源、MyBatis-Plus 和 Flyway 迁移链路真实可用。
 * 测试与应用使用同一套 Spring Config Data，并始终连接 TEST_DB_NAME 指定的测试库。
 * 未配置 TEST_DB_NAME 时跳过需要真实数据库的集成测试。
 */
@SpringBootTest
@EnabledIf(value = "testDatabaseConfigured",
        disabledReason = "未配置 TEST_DB_NAME 时跳过需要真实数据库的集成测试")
class ApplicationTests {

    private static final ConfigurableEnvironment CONFIGURATION = loadConfiguration();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void useTestDatabase(DynamicPropertyRegistry registry) {
        registry.add("DB_NAME", () -> CONFIGURATION.getRequiredProperty("TEST_DB_NAME"));
    }

    static boolean testDatabaseConfigured() {
        return StringUtils.hasText(CONFIGURATION.getProperty("TEST_DB_NAME"));
    }

    @Test
    void contextLoadsWithDatabase() {
        // 连接池是惰性的，必须真实执行一次 SQL 才能证明数据源可用
        Assertions.assertEquals(1, jdbcTemplate.queryForObject("SELECT 1", Integer.class));
    }

    private static ConfigurableEnvironment loadConfiguration() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        ConfigDataEnvironmentPostProcessor.applyTo(environment);
        return environment;
    }

}
