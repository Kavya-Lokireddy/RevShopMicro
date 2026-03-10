package com.revshop.checkout.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service")
public interface ProductServiceClient {

    @GetMapping("/api/internal/products/{productId}")
    Integer getProductStock(@PathVariable("productId") Long productId);

    @PutMapping("/api/internal/products/{productId}/stock")
    void updateProductStock(@PathVariable("productId") Long productId, @RequestParam("quantity") Integer quantity);
}
