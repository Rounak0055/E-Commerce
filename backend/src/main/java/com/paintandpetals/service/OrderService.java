package com.paintandpetals.service;

import com.paintandpetals.dto.request.OrderStatusUpdateRequest;
import com.paintandpetals.dto.response.OrderGroupResponse;
import com.paintandpetals.entity.OrderGroup;
import com.paintandpetals.entity.User;
import com.paintandpetals.entity.VendorOrder;
import com.paintandpetals.entity.enums.OrderStatus;
import com.paintandpetals.entity.enums.Role;
import com.paintandpetals.exception.BadRequestException;
import com.paintandpetals.exception.ResourceNotFoundException;
import com.paintandpetals.mapper.DtoMapper;
import com.paintandpetals.repository.OrderGroupRepository;
import com.paintandpetals.repository.VendorOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            OrderStatus.PLACED, EnumSet.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
            OrderStatus.PROCESSING, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
            OrderStatus.SHIPPED, EnumSet.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class),
            OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class),
            OrderStatus.PENDING_PAYMENT, EnumSet.of(OrderStatus.CANCELLED)
    );

    private final OrderGroupRepository orderGroupRepository;
    private final VendorOrderRepository vendorOrderRepository;
    private final DtoMapper mapper;

    @Transactional(readOnly = true)
    public List<OrderGroupResponse> getCustomerOrders(User customer) {
        return orderGroupRepository.findByCustomerOrderByCreatedAtDesc(customer).stream()
                .map(mapper::toOrderGroupResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderGroupResponse getOrder(User user, Long orderGroupId) {
        OrderGroup group = orderGroupRepository.findById(orderGroupId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (user.getRole() == Role.CUSTOMER && !group.getCustomer().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Order not found");
        }

        return mapper.toOrderGroupResponse(group);
    }

    @Transactional
    public VendorOrder updateVendorOrderStatus(User vendor, Long vendorOrderId, OrderStatusUpdateRequest request) {
        VendorOrder vendorOrder = vendorOrderRepository.findById(vendorOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!vendorOrder.getVendor().getId().equals(vendor.getId())) {
            throw new ResourceNotFoundException("Order not found");
        }

        validateTransition(vendorOrder.getStatus(), request.getStatus());
        vendorOrder.setStatus(request.getStatus());
        syncOrderGroupStatus(vendorOrder.getOrderGroup());
        return vendorOrderRepository.save(vendorOrder);
    }

    @Transactional
    public OrderGroup updateOrderGroupStatus(User admin, Long orderGroupId, OrderStatusUpdateRequest request) {
        OrderGroup group = orderGroupRepository.findById(orderGroupId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        for (VendorOrder vo : group.getVendorOrders()) {
            validateTransition(vo.getStatus(), request.getStatus());
            vo.setStatus(request.getStatus());
        }
        group.setStatus(request.getStatus());
        return orderGroupRepository.save(group);
    }

    private void syncOrderGroupStatus(OrderGroup group) {
        boolean allSame = group.getVendorOrders().stream()
                .map(VendorOrder::getStatus)
                .distinct()
                .count() == 1;
        if (allSame) {
            group.setStatus(group.getVendorOrders().get(0).getStatus());
        }
    }

    private void validateTransition(OrderStatus current, OrderStatus next) {
        Set<OrderStatus> allowed = TRANSITIONS.getOrDefault(current, EnumSet.noneOf(OrderStatus.class));
        if (!allowed.contains(next)) {
            throw new BadRequestException("Invalid status transition from " + current + " to " + next);
        }
    }
}
