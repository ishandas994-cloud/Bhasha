// backend/src/main/java/com/banglalang/api/WebConfig.java
package com.banglalang.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Central CORS configuration. The frontend (Vite dev server on
 * http://localhost:5173 in dev, a Vercel domain in production) and this
 * backend live on different origins as far as the browser is concerned,
 * so without this every fetch() call from the Run button would be blocked
 * by the browser before it even reaches RunController.
 *
 * The allowed origins come from the CORS_ALLOWED_ORIGINS environment
 * variable (comma-separated), so the same jar works locally and on any
 * host without code changes. Entries may be exact origins or wildcard
 * patterns - we use allowedOriginPatterns() rather than allowedOrigins()
 * because only the former understands patterns like "https://*.vercel.app".
 *
 * Kept in one place (rather than @CrossOrigin on individual controllers)
 * so every endpoint we add later automatically gets the same policy.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
