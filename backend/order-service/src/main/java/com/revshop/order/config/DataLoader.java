package com.revshop.order.config;

import com.revshop.order.entity.*;
import com.revshop.order.repository.NotificationRepository;
import com.revshop.order.repository.OrderRepository;
import com.revshop.order.repository.ReviewRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final OrderRepository orderRepository;
    private final NotificationRepository notificationRepository;
    private final ReviewRepository reviewRepository;

    public DataLoader(OrderRepository orderRepository, NotificationRepository notificationRepository, ReviewRepository reviewRepository) {
        this.orderRepository = orderRepository;
        this.notificationRepository = notificationRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public void run(String... args) {
        // Only load sample data if database is empty
        if (orderRepository.count() == 0) {
            loadSampleOrders();
        }
    }

    private void loadSampleOrders() {
        // Sample Order 1
        Order order1 = new Order();
        order1.setUserId(1L);
        order1.setTotalAmount(79.99);
        order1.setShippingAddress("123 Main St, New York, NY 10001");
        order1.setBillingAddress("123 Main St, New York, NY 10001");
        order1.setContactName("John Doe");
        order1.setPhoneNumber("1234567890");
        order1.setPaymentMethod("CREDIT_CARD");
        order1.setPaymentStatus("SUCCESS");
        order1.setOrderDate(LocalDateTime.now().minusDays(5));
        order1.setStatus(OrderStatus.DELIVERED);

        OrderItem item1 = new OrderItem();
        item1.setProductId(1L);
        item1.setProductName("Wireless Headphones");
        item1.setSellerId(2L);
        item1.setQuantity(1);
        item1.setPriceAtPurchase(79.99);
        item1.setSubtotal(79.99);
        order1.addOrderItem(item1);

        orderRepository.save(order1);

        // Sample Order 2
        Order order2 = new Order();
        order2.setUserId(3L);
        order2.setTotalAmount(39.98);
        order2.setShippingAddress("456 Oak Ave, Los Angeles, CA 90001");
        order2.setBillingAddress("456 Oak Ave, Los Angeles, CA 90001");
        order2.setContactName("Jane Smith");
        order2.setPhoneNumber("9876543210");
        order2.setPaymentMethod("DEBIT_CARD");
        order2.setPaymentStatus("SUCCESS");
        order2.setOrderDate(LocalDateTime.now().minusDays(2));
        order2.setStatus(OrderStatus.SHIPPED);

        OrderItem item2 = new OrderItem();
        item2.setProductId(3L);
        item2.setProductName("Men's T-Shirt");
        item2.setSellerId(4L);
        item2.setQuantity(2);
        item2.setPriceAtPurchase(19.99);
        item2.setSubtotal(39.98);
        order2.addOrderItem(item2);

        orderRepository.save(order2);

        // Sample Notifications
        Notification notification1 = new Notification(
                1L,
                "Your order #" + order1.getId() + " has been delivered!",
                NotificationType.ORDER_DELIVERED,
                order1.getId()
        );
        notificationRepository.save(notification1);

        Notification notification2 = new Notification(
                3L,
                "Your order #" + order2.getId() + " has been shipped!",
                NotificationType.ORDER_SHIPPED,
                order2.getId()
        );
        notificationRepository.save(notification2);

        // Sample Review
        Review review1 = new Review();
        review1.setUserId(1L);
        review1.setProductId(1L);
        review1.setOrderId(order1.getId());
        review1.setRating(5);
        review1.setComment("Excellent product! Highly recommended.");
        reviewRepository.save(review1);

        System.out.println("Sample orders, notifications, and reviews loaded successfully!");
    }
}
