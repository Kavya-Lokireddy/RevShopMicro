package com.revshop.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
public class CorsConfig {

    private static final String ALLOWED_ORIGIN = "http://localhost:4200";
    private static final String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS, PATCH";
    private static final String ALLOWED_HEADERS = "*";

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter corsFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {

            // For OPTIONS preflight, respond immediately with CORS headers
            if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.OK);
                HttpHeaders headers = response.getHeaders();
                headers.set("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
                headers.set("Access-Control-Allow-Methods", ALLOWED_METHODS);
                headers.set("Access-Control-Allow-Headers", ALLOWED_HEADERS);
                headers.set("Access-Control-Allow-Credentials", "true");
                headers.set("Access-Control-Max-Age", "3600");
                return Mono.empty();
            }

            // For actual requests, add CORS headers AFTER downstream processing
            // and remove any duplicate CORS headers from downstream services
            exchange.getResponse().beforeCommit(() -> {
                HttpHeaders headers = exchange.getResponse().getHeaders();
                // Remove any CORS headers added by downstream services
                headers.remove("Access-Control-Allow-Origin");
                headers.remove("Access-Control-Allow-Methods");
                headers.remove("Access-Control-Allow-Headers");
                headers.remove("Access-Control-Allow-Credentials");
                headers.remove("Access-Control-Expose-Headers");
                // Set our own CORS headers (single value)
                headers.set("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
                headers.set("Access-Control-Allow-Credentials", "true");
                headers.set("Access-Control-Expose-Headers", "Authorization");
                return Mono.empty();
            });

            return chain.filter(exchange);
        };
    }
}
