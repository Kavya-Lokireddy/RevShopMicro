package com.revshop.order.service.impl;

import com.revshop.order.client.ProductServiceClient;
import com.revshop.order.dto.CreateOrderRequest;
import com.revshop.order.dto.OrderResponse;
import com.revshop.order.dto.ProductDto;
import com.revshop.order.entity.*;
import com.revshop.order.exception.OrderNotFoundException;
import com.revshop.order.exception.UnauthorizedException;
import com.revshop.order.repository.OrderRepository;
import com.revshop.order.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private ProductDto testProduct;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        // Setup test product
        testProduct = new ProductDto();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(29.99);
        testProduct.setSellerId(10L);

        // Setup order item request
        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setProductName("Test Product");
        itemRequest.setSellerId(10L);
        itemRequest.setQuantity(2);
        itemRequest.setPriceAtPurchase(29.99);

        // Setup create order request
        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setUserId(1L);
        createOrderRequest.setTotalAmount(59.98);
        createOrderRequest.setShippingAddress("123 Main St");
        createOrderRequest.setBillingAddress("123 Main St");
        createOrderRequest.setContactName("John Doe");
        createOrderRequest.setPhoneNumber("1234567890");
        createOrderRequest.setPaymentMethod("COD");
        createOrderRequest.setPaymentStatus("PENDING");
        createOrderRequest.setItems(List.of(itemRequest));

        // Setup test order entity
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUserId(1L);
        testOrder.setTotalAmount(59.98);
        testOrder.setShippingAddress("123 Main St");
        testOrder.setBillingAddress("123 Main St");
        testOrder.setContactName("John Doe");
        testOrder.setPhoneNumber("1234567890");
        testOrder.setPaymentMethod("COD");
        testOrder.setPaymentStatus("PENDING");
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setCreatedAt(LocalDateTime.now());

        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setProductId(1L);
        orderItem.setProductName("Test Product");
        orderItem.setSellerId(10L);
        orderItem.setQuantity(2);
        orderItem.setPriceAtPurchase(29.99);
        orderItem.setSubtotal(59.98);
        orderItem.setOrder(testOrder);
        testOrder.setOrderItems(new ArrayList<>(List.of(orderItem)));
    }

    @Test
    @DisplayName("Test createOrder - should create order successfully")
    void testCreateOrder_Success() {
        when(productServiceClient.getProductById(1L)).thenReturn(testProduct);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderResponse response = orderService.createOrder(createOrderRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertNotNull(response.getItems());
        assertFalse(response.getItems().isEmpty());

        verify(orderRepository).save(any(Order.class));
        verify(notificationService, atLeastOnce()).createNotification(anyLong(), anyString(), any(NotificationType.class), anyLong());
    }

    @Test
    @DisplayName("Test createOrder - should throw exception when product not found")
    void testCreateOrder_ProductNotFound() {
        when(productServiceClient.getProductById(1L)).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> orderService.createOrder(createOrderRequest));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Test getOrderById - should return order when found")
    void testGetOrderById_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        OrderResponse response = orderService.getOrderById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals("John Doe", response.getContactName());
    }

    @Test
    @DisplayName("Test getOrderById - should throw exception when order not found")
    void testGetOrderById_NotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(999L));
    }

    @Test
    @DisplayName("Test getOrdersByBuyer - should return all orders for a buyer")
    void testGetOrdersByBuyer_Success() {
        when(orderRepository.findByUserIdOrderByOrderDateDesc(1L)).thenReturn(List.of(testOrder));

        List<OrderResponse> responses = orderService.getOrdersByBuyer(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getUserId());
    }

    @Test
    @DisplayName("Test getOrdersBySeller - should return orders for a seller")
    void testGetOrdersBySeller_Success() {
        when(orderRepository.findBySellerIdOrderByOrderDateDesc(10L)).thenReturn(List.of(testOrder));

        List<OrderResponse> responses = orderService.getOrdersBySeller(10L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    @DisplayName("Test updateOrderStatus - should update status and send notification")
    void testUpdateOrderStatus_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        testOrder.setStatus(OrderStatus.CONFIRMED);
        OrderResponse response = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        assertNotNull(response);
        verify(notificationService).createNotification(anyLong(), anyString(), any(NotificationType.class), anyLong());
    }

    @Test
    @DisplayName("Test cancelOrder - should cancel order successfully")
    void testCancelOrder_Success() {
        testOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        orderService.cancelOrder(1L, 1L);

        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository).save(any(Order.class));
        verify(notificationService, atLeastOnce()).createNotification(anyLong(), anyString(), any(NotificationType.class), anyLong());
    }

    @Test
    @DisplayName("Test cancelOrder - should throw exception when cancelling shipped order")
    void testCancelOrder_AlreadyShipped() {
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(1L, 1L));
    }
}
