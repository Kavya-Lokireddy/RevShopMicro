package com.revshop.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main application class for Auth Service.
 *
 * This microservice handles authentication and authorization for the RevShop platform.
 * Responsibilities:
 * - User registration and login
 * - JWT token generation and validation
 * - Password reset functionality
 * - User validation for API Gateway
 *
 * Port: 8081
 * Owner: Manjula (Login, Registration and Authentication)
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("  Auth Service Started Successfully!  ");
        System.out.println("  Port: 8081");
        System.out.println("  H2 Console: http://localhost:8081/h2-console");
        System.out.println("========================================");
    }
}
