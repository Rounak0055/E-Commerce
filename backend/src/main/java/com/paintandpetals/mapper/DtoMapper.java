package com.paintandpetals.mapper;

import com.paintandpetals.dto.response.*;
import com.paintandpetals.entity.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DtoMapper {

    public UserResponse toUserResponse(User user) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .vendorStatus(user.getVendorStatus());

        if (user.getVendorProfile() != null) {
            builder.businessName(user.getVendorProfile().getBusinessName());
        }
        return builder.build();
    }

    public ProductResponse toProductResponse(Product product) {
        String vendorName = product.getVendor().getVendorProfile() != null
                ? product.getVendor().getVendorProfile().getBusinessName()
                : product.getVendor().getFirstName() + " " + product.getVendor().getLastName();

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .stockQuantity(product.getStockQuantity())
                .shippingTerms(product.getShippingTerms())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .categorySlug(product.getCategory().getSlug())
                .vendorId(product.getVendor().getId())
                .vendorName(vendorName)
                .active(product.getActive())
                .build();
    }

    public CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .build();
    }

    public CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream().map(this::toCartItemResponse).collect(Collectors.toList());
        BigDecimal total = items.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .items(items)
                .total(total)
                .itemCount(items.stream().mapToInt(CartItemResponse::getQuantity).sum())
                .build();
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        Product p = item.getProduct();
        String vendorName = p.getVendor().getVendorProfile() != null
                ? p.getVendor().getVendorProfile().getBusinessName()
                : p.getVendor().getEmail();

        return CartItemResponse.builder()
                .id(item.getId())
                .productId(p.getId())
                .productName(p.getName())
                .imageUrl(p.getImageUrl())
                .unitPrice(p.getPrice())
                .quantity(item.getQuantity())
                .lineTotal(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .vendorId(p.getVendor().getId())
                .vendorName(vendorName)
                .build();
    }

    public OrderGroupResponse toOrderGroupResponse(OrderGroup group) {
        List<VendorOrderResponse> vendorOrders = group.getVendorOrders().stream()
                .map(this::toVendorOrderResponse)
                .collect(Collectors.toList());

        OrderGroupResponse.OrderGroupResponseBuilder builder = OrderGroupResponse.builder()
                .id(group.getId())
                .status(group.getStatus())
                .totalAmount(group.getTotalAmount())
                .shippingStreet(group.getShippingStreet())
                .shippingCity(group.getShippingCity())
                .shippingState(group.getShippingState())
                .shippingZip(group.getShippingZip())
                .shippingCountry(group.getShippingCountry())
                .shippingPhone(group.getShippingPhone())
                .createdAt(group.getCreatedAt())
                .vendorOrders(vendorOrders);

        if (group.getPayment() != null && group.getPayment().getRazorpayOrderId() != null) {
            builder.payment(PaymentInitResponse.builder()
                    .razorpayOrderId(group.getPayment().getRazorpayOrderId())
                    .amount(group.getPayment().getAmount())
                    .currency("INR")
                    .build());
        }

        return builder.build();
    }

    private VendorOrderResponse toVendorOrderResponse(VendorOrder vo) {
        String vendorName = vo.getVendor().getVendorProfile() != null
                ? vo.getVendor().getVendorProfile().getBusinessName()
                : vo.getVendor().getEmail();

        return VendorOrderResponse.builder()
                .id(vo.getId())
                .vendorId(vo.getVendor().getId())
                .vendorName(vendorName)
                .status(vo.getStatus())
                .subtotal(vo.getSubtotal())
                .items(vo.getItems().stream().map(i -> OrderItemResponse.builder()
                        .id(i.getId())
                        .productId(i.getProduct().getId())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build()).collect(Collectors.toList()))
                .build();
    }

    public VendorFulfillmentResponse toFulfillmentResponse(VendorOrder vo) {
        OrderGroup og = vo.getOrderGroup();
        User customer = og.getCustomer();

        return VendorFulfillmentResponse.builder()
                .vendorOrderId(vo.getId())
                .orderGroupId(og.getId())
                .status(vo.getStatus())
                .orderDate(og.getCreatedAt())
                .customerName(customer.getFirstName() + " " + customer.getLastName())
                .customerEmail(customer.getEmail())
                .shippingStreet(og.getShippingStreet())
                .shippingCity(og.getShippingCity())
                .shippingState(og.getShippingState())
                .shippingZip(og.getShippingZip())
                .shippingCountry(og.getShippingCountry())
                .shippingPhone(og.getShippingPhone())
                .subtotal(vo.getSubtotal())
                .items(vo.getItems().stream().map(i -> FulfillmentItemResponse.builder()
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build()).collect(Collectors.toList()))
                .build();
    }

    public BigDecimal calculateAov(BigDecimal revenue, long orderCount) {
        if (orderCount == 0) return BigDecimal.ZERO;
        return revenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP);
    }
}
