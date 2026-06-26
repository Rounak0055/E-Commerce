package com.paintandpetals.security;

import com.paintandpetals.entity.User;
import com.paintandpetals.entity.enums.Role;
import com.paintandpetals.entity.enums.VendorStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new IllegalStateException("No authenticated user");
        }
        return user;
    }

    public User getApprovedVendor() {
        User user = getCurrentUser();
        if (user.getRole() != Role.VENDOR || user.getVendorStatus() != VendorStatus.APPROVED) {
            throw new AccessDeniedException("Vendor account is not approved");
        }
        return user;
    }
}
