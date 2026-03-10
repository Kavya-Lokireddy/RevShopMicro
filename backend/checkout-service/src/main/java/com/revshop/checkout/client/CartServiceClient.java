package com.revshop.checkout.client;

import com.revshop.checkout.dto.CartDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "cart-service")
public interface CartServiceClient {

    @GetMapping("/api/cart")
    CartDto getCart(@RequestHeader("X-User-Id") Long userId);

    @DeleteMapping("/api/cart")
    void clearCart(@RequestHeader("X-User-Id") Long userId);
}
