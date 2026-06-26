package com.paintandpetals.repository;

import com.paintandpetals.entity.User;
import com.paintandpetals.entity.VendorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VendorProfileRepository extends JpaRepository<VendorProfile, Long> {
    Optional<VendorProfile> findByUserId(Long userId);
    Optional<VendorProfile> findByUser(User user);
}
