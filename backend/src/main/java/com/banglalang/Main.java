// backend/src/main/java/com/banglalang/Main.java
package com.banglalang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point. Spring Boot auto-discovers RunController (and WebConfig)
 * via component scanning since they live under this same com.banglalang
 * package tree - no manual bean registration needed.
 *
 * Run with: mvn spring-boot:run   (from the backend/ directory)
 * Server starts on http://localhost:8080 by default.
 */
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}