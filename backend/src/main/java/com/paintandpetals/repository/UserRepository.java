package com.paintandpetals.repository;

import com.paintandpetals.entity.User;
import com.paintandpetals.entity.enums.Role;
import com.paintandpetals.entity.enums.VendorStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRoleAndVendorStatus(Role role, VendorStatus vendorStatus);
    List<User> findByRole(Role role);
    List<User> findByRoleAndVendorStatus(Role role, VendorStatus vendorStatus);
}
