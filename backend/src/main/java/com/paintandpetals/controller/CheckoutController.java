package com.paintandpetals.controller;

import com.paintandpetals.dto.request.PaymentVerifyRequest;
import com.paintandpetals.dto.request.ShippingAddressRequest;
import com.paintandpetals.dto.response.OrderGroupResponse;
import com.paintandpetals.security.SecurityUtils;
import com.paintandpetals.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderGroupResponse checkout(@Valid @RequestBody ShippingAddressRequest request) {
        return checkoutService.checkout(securityUtils.getCurrentUser(), request);
    }

    @PostMapping("/verify-payment")
    public OrderGroupResponse verifyPayment(@Valid @RequestBody PaymentVerifyRequest request) {
        return checkoutService.verifyPayment(securityUtils.getCurrentUser(), request);
    }
}
