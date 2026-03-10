package com.revshop.order.service.impl;

import com.revshop.order.dto.CreateOrderRequest;
import com.revshop.order.dto.OrderItemResponse;
import com.revshop.order.dto.OrderResponse;
import com.revshop.order.entity.*;
import com.revshop.order.client.ProductServiceClient;
import com.revshop.order.dto.ProductDto;
import com.revshop.order.exception.OrderNotFoundException;
import com.revshop.order.exception.UnauthorizedException;
import com.revshop.order.repository.OrderRepository;
import com.revshop.order.service.NotificationService;
import com.revshop.order.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final ProductServiceClient productServiceClient;

    public OrderServiceImpl(OrderRepository orderRepository, NotificationService notificationService, ProductServiceClient productServiceClient) {
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
        this.productServiceClient = productServiceClient;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setShippingAddress(request.getShippingAddress());
        order.setBillingAddress(request.getBillingAddress());
        order.setContactName(request.getContactName());
        order.setPhoneNumber(request.getPhoneNumber());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus(request.getPaymentStatus());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        double calculatedTotalAmount = 0.0;

        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            ProductDto product = productServiceClient.getProductById(itemRequest.getProductId());
            if (product == null) {
                throw new IllegalStateException("Product not found: " + itemRequest.getProductId());
            }

            Double actualPrice = product.getPrice();
            Long actualSellerId = product.getSellerId();

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setSellerId(actualSellerId);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPriceAtPurchase(actualPrice);
            
            double subtotal = itemRequest.getQuantity() * actualPrice;
            orderItem.setSubtotal(subtotal);
            calculatedTotalAmount += subtotal;

            order.addOrderItem(orderItem);
        }

        order.setTotalAmount(calculatedTotalAmount);

        Order savedOrder = orderRepository.save(order);

        // Create notification for buyer
        notificationService.createNotification(
                savedOrder.getUserId(),
                "Your order #" + savedOrder.getId() + " has been placed successfully!",
                NotificationType.ORDER_PLACED,
                savedOrder.getId()
        );

        // Create notification for each seller
        savedOrder.getOrderItems().stream()
                .map(OrderItem::getSellerId)
                .distinct()
                .forEach(sellerId -> notificationService.createNotification(
                        sellerId,
                        "New order received! Order #" + savedOrder.getId(),
                        NotificationType.ORDER_PLACED,
                        savedOrder.getId()
                ));

        return mapToOrderResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        return mapToOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersByBuyer(Long userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId)
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersBySeller(Long sellerId) {
        return orderRepository.findBySellerIdOrderByOrderDateDesc(sellerId)
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        // Create notification based on status
        NotificationType notificationType = getNotificationTypeForStatus(status);
        String message = getStatusChangeMessage(orderId, status);

        notificationService.createNotification(
                order.getUserId(),
                message,
                notificationType,
                orderId
        );

        return mapToOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to cancel this order");
        }

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel order that is already " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Notify buyer
        notificationService.createNotification(
                order.getUserId(),
                "Your order #" + orderId + " has been cancelled",
                NotificationType.ORDER_CANCELLED,
                orderId
        );

        // Notify sellers
        order.getOrderItems().stream()
                .map(OrderItem::getSellerId)
                .distinct()
                .forEach(sellerId -> notificationService.createNotification(
                        sellerId,
                        "Order #" + orderId + " has been cancelled by the buyer",
                        NotificationType.ORDER_CANCELLED,
                        orderId
                ));
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getSellerId(),
                        item.getQuantity(),
                        item.getPriceAtPurchase(),
                        item.getSubtotal()
                ))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getBillingAddress(),
                order.getContactName(),
                order.getPhoneNumber(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getOrderDate(),
                order.getStatus(),
                order.getCreatedAt(),
                items
        );
    }

    private NotificationType getNotificationTypeForStatus(OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> NotificationType.ORDER_CONFIRMED;
            case SHIPPED -> NotificationType.ORDER_SHIPPED;
            case DELIVERED -> NotificationType.ORDER_DELIVERED;
            case CANCELLED -> NotificationType.ORDER_CANCELLED;
            default -> NotificationType.ORDER_PLACED;
        };
    }

    private String getStatusChangeMessage(Long orderId, OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> "Your order #" + orderId + " has been confirmed!";
            case SHIPPED -> "Your order #" + orderId + " has been shipped!";
            case DELIVERED -> "Your order #" + orderId + " has been delivered!";
            case CANCELLED -> "Your order #" + orderId + " has been cancelled";
            default -> "Order #" + orderId + " status updated to " + status;
        };
    }
}
