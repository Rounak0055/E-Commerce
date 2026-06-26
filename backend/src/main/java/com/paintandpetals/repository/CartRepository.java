package com.paintandpetals.repository;

import com.paintandpetals.entity.Cart;
import com.paintandpetals.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
