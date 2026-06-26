package com.paintandpetals.controller;

import com.paintandpetals.dto.response.OrderGroupResponse;
import com.paintandpetals.security.SecurityUtils;
import com.paintandpetals.service.CheckoutService;
import com.paintandpetals.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CheckoutService checkoutService;
    private final SecurityUtils securityUtils;

    @GetMapping("/orders")
    public List<OrderGroupResponse> myOrders() {
        return orderService.getCustomerOrders(securityUtils.getCurrentUser());
    }

    @GetMapping("/orders/{id}")
    public OrderGroupResponse getOrder(@PathVariable Long id) {
        return orderService.getOrder(securityUtils.getCurrentUser(), id);
    }

    @PostMapping("/payments/webhook")
    public void webhook(@RequestBody String payload, HttpServletRequest request) {
        String signature = request.getHeader("X-Razorpay-Signature");
        checkoutService.handleWebhook(payload, signature);
    }
}
