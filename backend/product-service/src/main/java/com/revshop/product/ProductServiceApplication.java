package com.revshop.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Product Service Application
 *
 * Microservice for managing products and categories in RevShop
 *
 * Port: 8082
 * Owners: Kavya (Seller Dashboard) + Jatin (Buyer Product Management)
 *
 * Features:
 * - Product CRUD operations for sellers
 * - Product browsing and search for buyers
 * - Category management
 * - Stock management and threshold alerts
 * - Internal APIs for order service integration
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
