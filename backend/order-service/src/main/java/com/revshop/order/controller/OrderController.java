package com.revshop.order.controller;

import com.revshop.order.dto.CreateOrderRequest;
import com.revshop.order.dto.OrderResponse;
import com.revshop.order.dto.UpdateOrderStatusRequest;
import com.revshop.order.service.OrderService;
import com.revshop.order.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        OrderResponse response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders(HttpServletRequest request) {
        Long userId = JwtUtil.getUserIdFromRequest(request);
        List<OrderResponse> orders = orderService.getOrdersByBuyer(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/seller")
    public ResponseEntity<List<OrderResponse>> getSellerOrders(HttpServletRequest request) {
        Long sellerId = JwtUtil.getUserIdFromRequest(request);
        List<OrderResponse> orders = orderService.getOrdersBySeller(sellerId);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse response = orderService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(
            @PathVariable Long orderId,
            HttpServletRequest request) {
        Long userId = JwtUtil.getUserIdFromRequest(request);
        orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok("Order cancelled successfully");
    }
}
