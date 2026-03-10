package com.revshop.order.service;

import com.revshop.order.dto.CreateOrderRequest;
import com.revshop.order.dto.OrderResponse;
import com.revshop.order.entity.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderById(Long orderId);

    List<OrderResponse> getOrdersByBuyer(Long userId);

    List<OrderResponse> getOrdersBySeller(Long sellerId);

    OrderResponse updateOrderStatus(Long orderId, OrderStatus status);

    void cancelOrder(Long orderId, Long userId);
}
