package com.paintandpetals.service;

import com.paintandpetals.dto.response.*;
import com.paintandpetals.dto.request.MarketplaceSettingsRequest;
import com.paintandpetals.entity.User;
import com.paintandpetals.entity.VendorOrder;
import com.paintandpetals.entity.enums.OrderStatus;
import com.paintandpetals.entity.enums.Role;
import com.paintandpetals.entity.enums.VendorStatus;
import com.paintandpetals.exception.BadRequestException;
import com.paintandpetals.exception.ResourceNotFoundException;
import com.paintandpetals.mapper.DtoMapper;
import com.paintandpetals.repository.CartRepository;
import com.paintandpetals.repository.CartItemRepository;
import com.paintandpetals.repository.OrderGroupRepository;
import com.paintandpetals.repository.ProductRepository;
import com.paintandpetals.repository.UserRepository;
import com.paintandpetals.repository.VendorOrderRepository;
import com.paintandpetals.repository.VendorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final OrderGroupRepository orderGroupRepository;
    private final VendorOrderRepository vendorOrderRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final DtoMapper mapper;
    private BigDecimal commissionRate = BigDecimal.valueOf(12.5);
    private boolean alertBannerEnabled = false;
    private String alertBannerMessage = "Fresh seasonal collections are now open.";

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        Instant startOfDay = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);

        BigDecimal totalRevenue = orderGroupRepository.sumTotalRevenue();
        long totalOrders = orderGroupRepository.countCompletedOrders();
        long activeVendors = userRepository.countByRoleAndVendorStatus(Role.VENDOR, VendorStatus.APPROVED);

        List<VendorBreakdownResponse> breakdown = userRepository
                .findByRole(Role.VENDOR).stream()
                .map(this::toVendorBreakdown)
                .collect(Collectors.toList());

        return AdminDashboardResponse.builder()
                .totalRevenue(totalRevenue)
                .averageOrderValue(mapper.calculateAov(totalRevenue, totalOrders))
                .activeVendorCount(activeVendors)
                .dailyOrderCount(orderGroupRepository.countOrdersSince(startOfDay))
                .dailyRevenue(orderGroupRepository.sumRevenueSince(startOfDay))
                .vendorBreakdown(breakdown)
                .customers(getCustomerDirectory())
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getPendingVendors() {
        return userRepository.findByRoleAndVendorStatus(Role.VENDOR, VendorStatus.PENDING).stream()
                .map(mapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getVendors() {
        return userRepository.findByRole(Role.VENDOR).stream()
                .map(mapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse approveVendor(Long vendorId) {
        User vendor = getVendor(vendorId);
        if (vendor.getVendorStatus() != VendorStatus.PENDING) {
            throw new BadRequestException("Vendor is not pending approval");
        }
        vendor.setVendorStatus(VendorStatus.APPROVED);
        if (vendor.getVendorProfile() != null) {
            vendor.getVendorProfile().setApprovedAt(Instant.now());
        }
        return mapper.toUserResponse(userRepository.save(vendor));
    }

    @Transactional
    public UserResponse rejectVendor(Long vendorId) {
        User vendor = getVendor(vendorId);
        vendor.setVendorStatus(VendorStatus.REJECTED);
        return mapper.toUserResponse(userRepository.save(vendor));
    }

    @Transactional(readOnly = true)
    public List<CustomerDirectoryResponse> getCustomerDirectory() {
        return userRepository.findByRole(Role.CUSTOMER).stream()
                .map(this::toCustomerDirectory)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MarketplaceSettingsResponse getMarketplaceSettings() {
        return MarketplaceSettingsResponse.builder()
                .commissionRate(commissionRate)
                .alertBannerEnabled(alertBannerEnabled)
                .alertBannerMessage(alertBannerMessage)
                .build();
    }

    @Transactional
    public MarketplaceSettingsResponse updateMarketplaceSettings(MarketplaceSettingsRequest request) {
        if (request.getCommissionRate() != null) {
            commissionRate = request.getCommissionRate();
        }
        if (request.getAlertBannerEnabled() != null) {
            alertBannerEnabled = request.getAlertBannerEnabled();
        }
        if (request.getAlertBannerMessage() != null) {
            alertBannerMessage = request.getAlertBannerMessage().trim();
        }
        return getMarketplaceSettings();
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be removed");
        }

        if (user.getRole() == Role.VENDOR) {
            cartItemRepository.deleteByProductVendor(user);
            cartItemRepository.flush();
            vendorOrderRepository.findByVendorOrderByIdDesc(user).forEach(vendorOrderRepository::delete);
            vendorOrderRepository.flush();
            productRepository.deleteAll(productRepository.findByVendor(user));
            productRepository.flush();
            vendorProfileRepository.findByUser(user).ifPresent(vendorProfileRepository::delete);
        }

        if (user.getRole() == Role.CUSTOMER) {
            orderGroupRepository.findByCustomerOrderByCreatedAtDesc(user).forEach(orderGroupRepository::delete);
        }

        cartRepository.findByUser(user).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
            cartRepository.delete(cart);
        });

        userRepository.deleteById(userId);
    }

    private VendorBreakdownResponse toVendorBreakdown(User vendor) {
        Long vendorId = vendor.getId();
        BigDecimal revenue = vendorOrderRepository.sumTotalRevenueByVendor(vendorId);
        long orders = vendorOrderRepository.countActiveOrdersByVendor(vendorId);
        String name = vendor.getVendorProfile() != null
                ? vendor.getVendorProfile().getBusinessName()
                : vendor.getFirstName() + " " + vendor.getLastName();

        return VendorBreakdownResponse.builder()
                .vendorId(vendorId)
                .vendorName(name)
                .email(vendor.getEmail())
                .vendorStatus(vendor.getVendorStatus())
                .activeOrders(orders)
                .totalRevenue(revenue)
                .averageOrderValue(mapper.calculateAov(revenue, orders))
                .build();
    }

    private CustomerDirectoryResponse toCustomerDirectory(User customer) {
        List<com.paintandpetals.entity.OrderGroup> orders = orderGroupRepository
                .findByCustomerOrderByCreatedAtDesc(customer);

        BigDecimal totalSpent = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.PENDING_PAYMENT && o.getStatus() != OrderStatus.CANCELLED)
                .map(com.paintandpetals.entity.OrderGroup::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long orderCount = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.PENDING_PAYMENT && o.getStatus() != OrderStatus.CANCELLED)
                .count();

        return CustomerDirectoryResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .totalSpent(totalSpent)
                .orderCount(orderCount)
                .build();
    }

    private User getVendor(Long vendorId) {
        User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        if (vendor.getRole() != Role.VENDOR) {
            throw new BadRequestException("User is not a vendor");
        }
        return vendor;
    }
}
