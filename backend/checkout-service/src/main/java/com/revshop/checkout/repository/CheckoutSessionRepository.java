package com.revshop.checkout.repository;

import com.revshop.checkout.entity.CheckoutSession;
import com.revshop.checkout.entity.CheckoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckoutSessionRepository extends JpaRepository<CheckoutSession, Long> {

    Optional<CheckoutSession> findByIdAndUserId(Long id, Long userId);

    List<CheckoutSession> findByUserId(Long userId);

    List<CheckoutSession> findByStatusAndExpiresAtBefore(CheckoutStatus status, LocalDateTime expiresAt);
}
