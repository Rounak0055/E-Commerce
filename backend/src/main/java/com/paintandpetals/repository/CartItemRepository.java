package com.paintandpetals.repository;

import com.paintandpetals.entity.CartItem;
import com.paintandpetals.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.product IN (SELECT p FROM Product p WHERE p.vendor = :vendor)")
    void deleteByProductVendor(@Param("vendor") User vendor);
}
