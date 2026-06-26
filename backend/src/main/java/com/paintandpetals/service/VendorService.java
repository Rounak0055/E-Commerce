package com.paintandpetals.service;

import com.paintandpetals.dto.request.VendorProfileRequest;
import com.paintandpetals.dto.response.*;
import com.paintandpetals.entity.User;
import com.paintandpetals.entity.VendorOrder;
import com.paintandpetals.entity.VendorProfile;
import com.paintandpetals.entity.enums.OrderStatus;
import com.paintandpetals.mapper.DtoMapper;
import com.paintandpetals.repository.ProductRepository;
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
public class VendorService {

    private final VendorOrderRepository vendorOrderRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final ProductRepository productRepository;
    private final DtoMapper mapper;

    @Transactional(readOnly = true)
    public List<VendorFulfillmentResponse> getFulfillmentOrders(User vendor) {
        return vendorOrderRepository.findByVendorWithDetails(vendor).stream()
                .filter(vo -> vo.getStatus() != OrderStatus.PENDING_PAYMENT)
                .map(mapper::toFulfillmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VendorAnalyticsResponse getAnalytics(User vendor) {
        Instant now = Instant.now();
        Instant startOfDay = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant startOfWeek = startOfDay.minusSeconds(7 * 24 * 3600L);
        Instant startOfMonth = startOfDay.minusSeconds(30L * 24 * 3600);

        Long vendorId = vendor.getId();
        BigDecimal daily = vendorOrderRepository.sumSubtotalByVendorSince(vendorId, startOfDay);
        BigDecimal weekly = vendorOrderRepository.sumSubtotalByVendorSince(vendorId, startOfWeek);
        BigDecimal monthly = vendorOrderRepository.sumSubtotalByVendorSince(vendorId, startOfMonth);

        List<SalesDataPoint> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now(ZoneOffset.UTC).minusDays(i);
            Instant dayStart = date.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant dayEnd = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            BigDecimal amount = sumBetween(vendor, dayStart, dayEnd);
            trend.add(SalesDataPoint.builder()
                    .date(date.toString())
                    .amount(amount)
                    .build());
        }

        long pendingCount = vendorOrderRepository.countByVendorAndStatus(vendor, OrderStatus.PLACED);
        long shippedCount = vendorOrderRepository.countByVendorAndStatus(vendor, OrderStatus.SHIPPED);
        long cancelledCount = vendorOrderRepository.countByVendorAndStatus(vendor, OrderStatus.CANCELLED);
        long completedCount = vendorOrderRepository.countByVendorAndStatus(vendor, OrderStatus.DELIVERED);
        List<String> outOfStockProducts = productRepository.findByVendorAndActiveTrue(vendor).stream()
                .filter(product -> product.getStockQuantity() <= 0)
                .map(product -> product.getName())
                .limit(5)
                .toList();

        return VendorAnalyticsResponse.builder()
                .dailySales(daily)
                .weeklySales(weekly)
                .monthlySales(monthly)
                .salesTrend(trend)
                .pendingCount(pendingCount)
                .shippedCount(shippedCount)
                .cancelledCount(cancelledCount)
                .completedCount(completedCount)
                .outOfStockProducts(outOfStockProducts)
                .build();
    }

    @Transactional(readOnly = true)
    public List<VendorCustomerInsightResponse> getCustomerInsights(User vendor) {
        List<VendorOrder> orders = vendorOrderRepository.findByVendorOrderByIdDesc(vendor).stream()
                .filter(vo -> vo.getStatus() != OrderStatus.PENDING_PAYMENT && vo.getStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());

        Map<Long, VendorCustomerInsightResponse> map = new LinkedHashMap<>();

        for (VendorOrder vo : orders) {
            User customer = vo.getOrderGroup().getCustomer();
            map.compute(customer.getId(), (id, existing) -> {
                if (existing == null) {
                    return VendorCustomerInsightResponse.builder()
                            .customerId(customer.getId())
                            .name(customer.getFirstName() + " " + customer.getLastName())
                            .email(customer.getEmail())
                            .phone(customer.getPhone())
                            .totalSpent(vo.getSubtotal())
                            .orderCount(1)
                            .build();
                }
                existing.setTotalSpent(existing.getTotalSpent().add(vo.getSubtotal()));
                existing.setOrderCount(existing.getOrderCount() + 1);
                return existing;
            });
        }

        return new ArrayList<>(map.values());
    }

    @Transactional
    public VendorProfileResponse updateProfile(User vendor, VendorProfileRequest request) {
        VendorProfile profile = vendorProfileRepository.findByUser(vendor)
                .orElseGet(() -> {
                    VendorProfile newProfile = new VendorProfile();
                    newProfile.setUser(vendor);
                    newProfile.setBusinessName(request.getBusinessName());
                    return newProfile;
                });

        profile.setBusinessName(request.getBusinessName());
        profile.setDescription(request.getDescription());
        profile.setBannerUrl(request.getBannerUrl());
        profile.setLogoUrl(request.getLogoUrl());
        profile.setBio(request.getBio());

        VendorProfile saved = vendorProfileRepository.save(profile);
        return VendorProfileResponse.builder()
                .id(saved.getId())
                .businessName(saved.getBusinessName())
                .description(saved.getDescription())
                .bannerUrl(saved.getBannerUrl())
                .logoUrl(saved.getLogoUrl())
                .bio(saved.getBio())
                .build();
    }

    @Transactional(readOnly = true)
    public VendorProfileResponse getProfile(User vendor) {
        return vendorProfileRepository.findByUser(vendor)
                .map(profile -> VendorProfileResponse.builder()
                        .id(profile.getId())
                        .businessName(profile.getBusinessName())
                        .description(profile.getDescription())
                        .bannerUrl(profile.getBannerUrl())
                        .logoUrl(profile.getLogoUrl())
                        .bio(profile.getBio())
                        .build())
                .orElseGet(() -> VendorProfileResponse.builder().businessName(vendor.getFirstName() + " " + vendor.getLastName()).build());
    }

    @Transactional(readOnly = true)
    public VendorEarningsResponse getEarnings(User vendor) {
        BigDecimal settledRevenue = vendorOrderRepository.findByVendorOrderByIdDesc(vendor).stream()
                .filter(vo -> vo.getStatus() == OrderStatus.DELIVERED)
                .map(VendorOrder::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingPayout = vendorOrderRepository.findByVendorOrderByIdDesc(vendor).stream()
                .filter(vo -> vo.getStatus() != OrderStatus.DELIVERED && vo.getStatus() != OrderStatus.CANCELLED && vo.getStatus() != OrderStatus.PENDING_PAYMENT)
                .map(VendorOrder::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return VendorEarningsResponse.builder()
                .settledRevenue(settledRevenue)
                .pendingPayout(pendingPayout)
                .availableBalance(settledRevenue)
                .build();
    }

    private BigDecimal sumBetween(User vendor, Instant start, Instant end) {
        return vendorOrderRepository.findByVendorOrderByIdDesc(vendor).stream()
                .filter(vo -> vo.getStatus() != OrderStatus.PENDING_PAYMENT && vo.getStatus() != OrderStatus.CANCELLED)
                .filter(vo -> {
                    Instant created = vo.getOrderGroup().getCreatedAt();
                    return !created.isBefore(start) && created.isBefore(end);
                })
                .map(VendorOrder::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
