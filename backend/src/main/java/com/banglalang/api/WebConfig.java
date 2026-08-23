// backend/src/main/java/com/banglalang/api/WebConfig.java
package com.banglalang.api;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Central CORS configuration. The frontend (Vite dev server, typically
 * http://localhost:5173) and this backend (http://localhost:8080) are
 * different origins as far as the browser is concerned, so without this,
 * every fetch() call from CodeEditor's "Run" button would be blocked by
 * the browser before it even reaches RunController.
 *
 * Kept in one place (rather than @CrossOrigin on individual controllers)
 * so every endpoint we add later automatically gets the same policy.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:5173",  // Vite default dev port
                        "http://127.0.0.1:5173"
                )
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}