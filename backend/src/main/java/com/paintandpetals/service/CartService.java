package com.paintandpetals.service;

import com.paintandpetals.dto.request.CartItemRequest;
import com.paintandpetals.dto.response.CartResponse;
import com.paintandpetals.entity.*;
import com.paintandpetals.exception.BadRequestException;
import com.paintandpetals.exception.ResourceNotFoundException;
import com.paintandpetals.mapper.DtoMapper;
import com.paintandpetals.repository.CartRepository;
import com.paintandpetals.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final DtoMapper mapper;

    @Transactional(readOnly = true)
    public CartResponse getCart(User customer) {
        Cart cart = getOrCreateCart(customer);
        return mapper.toCartResponse(cart);
    }

    @Transactional
    public CartResponse addItem(User customer, CartItemRequest request) {
        Cart cart = getOrCreateCart(customer);
        Product product = productRepository.findById(request.getProductId())
                .filter(Product::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock");
        }

        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(item);
        }

        return mapper.toCartResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateItem(User customer, Long itemId, CartItemRequest request) {
        Cart cart = getOrCreateCart(customer);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        Product product = item.getProduct();
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock");
        }

        item.setQuantity(request.getQuantity());
        return mapper.toCartResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(User customer, Long itemId) {
        Cart cart = getOrCreateCart(customer);
        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        return mapper.toCartResponse(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(User customer) {
        Cart cart = getOrCreateCart(customer);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(User customer) {
        return cartRepository.findByUser(customer).orElseGet(() ->
                cartRepository.save(Cart.builder().user(customer).build()));
    }
}
