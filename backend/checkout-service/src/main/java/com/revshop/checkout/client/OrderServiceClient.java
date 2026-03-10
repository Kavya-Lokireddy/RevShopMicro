package com.revshop.checkout.client;

import com.revshop.checkout.dto.CreateOrderRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "order-service")
public interface OrderServiceClient {

    @PostMapping("/api/orders")
    Map<String, Object> createOrder(@RequestBody CreateOrderRequest request);
}
