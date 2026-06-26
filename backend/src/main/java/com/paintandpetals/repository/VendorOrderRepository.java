package com.paintandpetals.repository;

import com.paintandpetals.entity.User;
import com.paintandpetals.entity.VendorOrder;
import com.paintandpetals.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface VendorOrderRepository extends JpaRepository<VendorOrder, Long> {

    List<VendorOrder> findByVendorOrderByIdDesc(User vendor);

    @Query("SELECT vo FROM VendorOrder vo JOIN FETCH vo.orderGroup og JOIN FETCH vo.items WHERE vo.vendor = :vendor ORDER BY vo.id DESC")
    List<VendorOrder> findByVendorWithDetails(@Param("vendor") User vendor);

    @Query("SELECT COALESCE(SUM(vo.subtotal), 0) FROM VendorOrder vo WHERE vo.vendor.id = :vendorId AND vo.orderGroup.createdAt >= :start AND vo.status NOT IN ('PENDING_PAYMENT', 'CANCELLED')")
    BigDecimal sumSubtotalByVendorSince(@Param("vendorId") Long vendorId, @Param("start") Instant start);

    @Query("SELECT COUNT(vo) FROM VendorOrder vo WHERE vo.vendor.id = :vendorId AND vo.status NOT IN ('PENDING_PAYMENT', 'CANCELLED')")
    long countActiveOrdersByVendor(@Param("vendorId") Long vendorId);

    @Query("SELECT COALESCE(SUM(vo.subtotal), 0) FROM VendorOrder vo WHERE vo.vendor.id = :vendorId AND vo.status NOT IN ('PENDING_PAYMENT', 'CANCELLED')")
    BigDecimal sumTotalRevenueByVendor(@Param("vendorId") Long vendorId);

    long countByVendorAndStatus(User vendor, OrderStatus status);

    List<VendorOrder> findByVendorAndStatusNotIn(User vendor, List<OrderStatus> statuses);
}
