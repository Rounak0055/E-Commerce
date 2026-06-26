package com.paintandpetals.controller;

import com.paintandpetals.dto.request.CartItemRequest;
import com.paintandpetals.dto.response.CartResponse;
import com.paintandpetals.security.SecurityUtils;
import com.paintandpetals.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public CartResponse getCart() {
        return cartService.getCart(securityUtils.getCurrentUser());
    }

    @PostMapping("/items")
    public CartResponse addItem(@Valid @RequestBody CartItemRequest request) {
        return cartService.addItem(securityUtils.getCurrentUser(), request);
    }

    @PutMapping("/items/{itemId}")
    public CartResponse updateItem(@PathVariable Long itemId, @Valid @RequestBody CartItemRequest request) {
        return cartService.updateItem(securityUtils.getCurrentUser(), itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    public CartResponse removeItem(@PathVariable Long itemId) {
        return cartService.removeItem(securityUtils.getCurrentUser(), itemId);
    }
}
