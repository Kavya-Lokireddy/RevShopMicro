package com.revshop.apigateway.config;

import com.revshop.apigateway.filter.JwtAuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public RouteConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service - No JWT required
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("lb://auth-service"))

                // Product Service - Public product browsing
                .route("product-service", r -> r
                        .path("/api/products/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://product-service"))

                // Product categories - Requires JWT
                .route("category-service", r -> r
                        .path("/api/categories/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://product-service"))

                // Seller Product Management - Requires JWT
                .route("seller-service", r -> r
                        .path("/api/seller/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://product-service"))

                // Cart Service - Requires JWT
                .route("cart-service", r -> r
                        .path("/api/cart/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://cart-service"))

                // Favorites Service - Requires JWT
                .route("favorites-service", r -> r
                        .path("/api/favorites/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://cart-service"))

                // Checkout Service - Requires JWT
                .route("checkout-service", r -> r
                        .path("/api/checkout/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://checkout-service"))

                // Payment Service - Requires JWT
                .route("payment-service", r -> r
                        .path("/api/payment/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://checkout-service"))

                // Order Service - Requires JWT
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://order-service"))

                // Review Service - Requires JWT
                .route("review-service", r -> r
                        .path("/api/reviews/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://order-service"))

                // Notification Service - Requires JWT
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://order-service"))

                .build();
    }
}
