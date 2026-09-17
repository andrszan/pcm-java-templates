package com.example.app.common;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS：来源来自 CORS_ORIGINS（逗号分隔），缺省=关闭；
 * 始终不允许携带凭据。跨域行为变化前先验证错误响应与 X-Request-ID 头。
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final List<String> origins;

    public CorsConfig(org.springframework.core.env.Environment environment) {
        this.origins = List.of(environment.getProperty("cors.origins", "").split(","))
                .stream()
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        if (origins.isEmpty()) {
            return;
        }
        registry.addMapping("/**")
                .allowedOrigins(origins.toArray(String[]::new))
                .allowedMethods("*")
                .allowedHeaders("*")
                .exposedHeaders("X-Request-ID")
                .allowCredentials(false);
    }

}
