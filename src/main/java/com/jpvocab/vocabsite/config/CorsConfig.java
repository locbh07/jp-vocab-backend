package com.jpvocab.vocabsite.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Value("${app.env:1}")
    private int appEnv;

    @Value("${app.cors.origin.local:http://localhost:5173}")
    private String localOrigin;

    @Value("${app.cors.origin.render:https://jp-vocab-frontend-p38r.vercel.app}")
    private String renderOrigin;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        final String allowedOrigin = (appEnv == 2) ? renderOrigin : localOrigin;
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigin)
                        .allowedMethods(
                                "GET",
                                "POST",
                                "PUT",
                                "PATCH",   // 🔥 BẮT BUỘC
                                "DELETE",
                                "OPTIONS"
                        )
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
