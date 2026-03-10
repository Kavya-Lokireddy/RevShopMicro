package com.revshop.order.repository;

import com.revshop.order.entity.Order;
import com.revshop.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    List<Order> findByStatusOrderByOrderDateDesc(OrderStatus status);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE oi.sellerId = :sellerId ORDER BY o.orderDate DESC")
    List<Order> findBySellerIdOrderByOrderDateDesc(@Param("sellerId") Long sellerId);
}
