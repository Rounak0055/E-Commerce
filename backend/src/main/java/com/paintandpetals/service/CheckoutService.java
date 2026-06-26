package com.paintandpetals.service;

import com.paintandpetals.config.RazorpayProperties;
import com.paintandpetals.dto.request.PaymentVerifyRequest;
import com.paintandpetals.dto.request.ShippingAddressRequest;
import com.paintandpetals.dto.response.CartItemResponse;
import com.paintandpetals.dto.response.CartResponse;
import com.paintandpetals.entity.*;
import com.paintandpetals.entity.enums.OrderStatus;
import com.paintandpetals.entity.enums.PaymentStatus;
import com.paintandpetals.exception.BadRequestException;
import com.paintandpetals.exception.ConflictException;
import com.paintandpetals.exception.ResourceNotFoundException;
import com.paintandpetals.mapper.DtoMapper;
import com.paintandpetals.repository.OrderGroupRepository;
import com.paintandpetals.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paintandpetals.dto.response.OrderGroupResponse;
import com.paintandpetals.dto.response.PaymentInitResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutService {

    private final CartService cartService;
    private final OrderGroupRepository orderGroupRepository;
    private final PaymentRepository paymentRepository;
    private final InventoryService inventoryService;
    private final DtoMapper mapper;
    private final RazorpayProperties razorpayProperties;

    @Transactional
    public OrderGroupResponse checkout(User customer, ShippingAddressRequest address) {
        CartResponse cart = cartService.getCart(customer);
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Map<Long, List<CartItemResponse>> byVendor = cart.getItems().stream()
                .collect(Collectors.groupingBy(CartItemResponse::getVendorId));

        OrderGroup orderGroup = OrderGroup.builder()
                .customer(customer)
                .status(OrderStatus.PENDING_PAYMENT)
                .totalAmount(cart.getTotal())
                .shippingStreet(address.getStreet())
                .shippingCity(address.getCity())
                .shippingState(address.getState())
                .shippingZip(address.getZip())
                .shippingCountry(address.getCountry())
                .shippingPhone(address.getPhone())
                .build();

        List<VendorOrder> vendorOrders = new ArrayList<>();

        try {
            for (Map.Entry<Long, List<CartItemResponse>> entry : byVendor.entrySet()) {
                User vendor = inventoryService.getVendor(entry.getKey());
                BigDecimal subtotal = entry.getValue().stream()
                        .map(CartItemResponse::getLineTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                VendorOrder vendorOrder = VendorOrder.builder()
                        .orderGroup(orderGroup)
                        .vendor(vendor)
                        .status(OrderStatus.PENDING_PAYMENT)
                        .subtotal(subtotal)
                        .build();

                List<OrderItem> orderItems = new ArrayList<>();
                for (var cartItem : entry.getValue()) {
                    Product product = inventoryService.lockAndReserveStock(cartItem.getProductId(), cartItem.getQuantity());
                    OrderItem orderItem = OrderItem.builder()
                            .vendorOrder(vendorOrder)
                            .product(product)
                            .productName(product.getName())
                            .quantity(cartItem.getQuantity())
                            .unitPrice(product.getPrice())
                            .build();
                    orderItems.add(orderItem);
                }
                vendorOrder.setItems(orderItems);
                vendorOrders.add(vendorOrder);
            }
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ConflictException("Product stock changed during checkout. Please review your cart.");
        }

        orderGroup.setVendorOrders(vendorOrders);
        orderGroup = orderGroupRepository.save(orderGroup);

        Payment payment = createRazorpayPayment(orderGroup);
        orderGroup.setPayment(payment);

        cartService.clearCart(customer);

        OrderGroupResponse response = mapper.toOrderGroupResponse(orderGroup);
        response.setPayment(PaymentInitResponse.builder()
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayKeyId(razorpayProperties.getKeyId())
                .amount(payment.getAmount())
                .currency("INR")
                .build());
        return response;
    }

    @Transactional
    public OrderGroupResponse verifyPayment(User customer, PaymentVerifyRequest request) {
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        OrderGroup orderGroup = payment.getOrderGroup();
        if (!orderGroup.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("Unauthorized payment verification");
        }

        if (payment.getStatus() == PaymentStatus.VERIFIED) {
            return mapper.toOrderGroupResponse(orderGroup);
        }

        boolean valid = verifySignature(request);
        if (!valid) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new BadRequestException("Invalid payment signature");
        }

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setStatus(PaymentStatus.VERIFIED);
        payment.setVerifiedAt(Instant.now());
        paymentRepository.save(payment);

        orderGroup.setStatus(OrderStatus.PLACED);
        orderGroup.getVendorOrders().forEach(vo -> vo.setStatus(OrderStatus.PLACED));
        orderGroupRepository.save(orderGroup);

        return mapper.toOrderGroupResponse(orderGroup);
    }

    @Transactional
    public void handleWebhook(String payload, String signature) {
        if (razorpayProperties.getWebhookSecret() == null || razorpayProperties.getWebhookSecret().isBlank()) {
            log.warn("Webhook secret not configured, skipping webhook verification");
            return;
        }

        try {
            Utils.verifyWebhookSignature(payload, signature, razorpayProperties.getWebhookSecret());
        } catch (RazorpayException e) {
            throw new BadRequestException("Invalid webhook signature");
        }

        JSONObject event = new JSONObject(payload);
        if ("payment.captured".equals(event.getString("event"))) {
            JSONObject paymentEntity = event.getJSONObject("payload")
                    .getJSONObject("payment").getJSONObject("entity");
            String razorpayOrderId = paymentEntity.getString("order_id");

            paymentRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(payment -> {
                if (payment.getStatus() != PaymentStatus.VERIFIED) {
                    payment.setRazorpayPaymentId(paymentEntity.getString("id"));
                    payment.setStatus(PaymentStatus.VERIFIED);
                    payment.setVerifiedAt(Instant.now());
                    paymentRepository.save(payment);

                    OrderGroup og = payment.getOrderGroup();
                    og.setStatus(OrderStatus.PLACED);
                    og.getVendorOrders().forEach(vo -> vo.setStatus(OrderStatus.PLACED));
                    orderGroupRepository.save(og);
                }
            });
        }
    }

    private Payment createRazorpayPayment(OrderGroup orderGroup) {
        Payment payment = Payment.builder()
                .orderGroup(orderGroup)
                .amount(orderGroup.getTotalAmount())
                .status(PaymentStatus.CREATED)
                .build();

        if (razorpayProperties.getKeyId() == null || razorpayProperties.getKeyId().isBlank()) {
            payment.setRazorpayOrderId("mock_order_" + System.currentTimeMillis());
            payment.setStatus(PaymentStatus.PENDING);
            return paymentRepository.save(payment);
        }

        try {
            RazorpayClient client = new RazorpayClient(razorpayProperties.getKeyId(), razorpayProperties.getKeySecret());
            JSONObject options = new JSONObject();
            options.put("amount", orderGroup.getTotalAmount().multiply(BigDecimal.valueOf(100)).intValue());
            options.put("currency", "INR");
            options.put("receipt", "og_" + orderGroup.getId());

            Order razorpayOrder = client.orders.create(options);
            payment.setRazorpayOrderId(razorpayOrder.get("id"));
            payment.setStatus(PaymentStatus.PENDING);
        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed", e);
            throw new BadRequestException("Payment initialization failed: " + e.getMessage());
        }

        return paymentRepository.save(payment);
    }

    private boolean verifySignature(PaymentVerifyRequest request) {
        if (razorpayProperties.getKeySecret() == null || razorpayProperties.getKeySecret().isBlank()) {
            return request.getRazorpayOrderId().startsWith("mock_order_");
        }

        try {
            String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
            Utils.verifyPaymentSignature(
                    new JSONObject()
                            .put("razorpay_order_id", request.getRazorpayOrderId())
                            .put("razorpay_payment_id", request.getRazorpayPaymentId())
                            .put("razorpay_signature", request.getRazorpaySignature()),
                    razorpayProperties.getKeySecret());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
