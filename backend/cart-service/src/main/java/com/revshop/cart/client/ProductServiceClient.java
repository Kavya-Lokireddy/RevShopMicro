package com.revshop.cart.client;

import com.revshop.cart.dto.ProductDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductServiceClient {

    @GetMapping("/api/products/{id}")
    @CircuitBreaker(name = "product-service", fallbackMethod = "getProductFallback")
    ProductDto getProduct(@PathVariable("id") Long id);

    @GetMapping("/api/internal/products/{id}/stock")
    @CircuitBreaker(name = "product-service", fallbackMethod = "checkStockFallback")
    Boolean checkStock(@PathVariable("id") Long id, Integer quantity);

    // Fallback methods
    default ProductDto getProductFallback(Long id, Exception ex) {
        ProductDto fallback = new ProductDto();
        fallback.setId(id);
        fallback.setName("Product Unavailable");
        fallback.setDescription("Product service is currently unavailable");
        fallback.setPrice(0.0);
        fallback.setQuantity(0);
        return fallback;
    }

    default Boolean checkStockFallback(Long id, Integer quantity, Exception ex) {
        // Conservative fallback: return false if service is unavailable
        return false;
    }
}
