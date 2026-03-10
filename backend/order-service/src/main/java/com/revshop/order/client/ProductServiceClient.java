package com.revshop.order.client;

import com.revshop.order.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service")
public interface ProductServiceClient {

    @PutMapping("/api/internal/products/{productId}/rating")
    void updateProductRating(@PathVariable("productId") Long productId, @RequestParam("rating") Double rating);

    @GetMapping("/api/internal/products/{id}")
    ProductDto getProductById(@PathVariable("id") Long id);
}
