package com.paintandpetals.repository;

import com.paintandpetals.entity.OrderGroup;
import com.paintandpetals.entity.User;
import com.paintandpetals.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface OrderGroupRepository extends JpaRepository<OrderGroup, Long> {
    List<OrderGroup> findByCustomerOrderByCreatedAtDesc(User customer);

    @Query("SELECT COUNT(og) FROM OrderGroup og WHERE og.createdAt >= :start AND og.status NOT IN ('PENDING_PAYMENT', 'CANCELLED')")
    long countOrdersSince(@Param("start") Instant start);

    @Query("SELECT COALESCE(SUM(og.totalAmount), 0) FROM OrderGroup og WHERE og.createdAt >= :start AND og.status NOT IN ('PENDING_PAYMENT', 'CANCELLED')")
    BigDecimal sumRevenueSince(@Param("start") Instant start);

    @Query("SELECT COALESCE(SUM(og.totalAmount), 0) FROM OrderGroup og WHERE og.status NOT IN ('PENDING_PAYMENT', 'CANCELLED')")
    BigDecimal sumTotalRevenue();

    @Query("SELECT COUNT(og) FROM OrderGroup og WHERE og.status NOT IN ('PENDING_PAYMENT', 'CANCELLED')")
    long countCompletedOrders();
}
