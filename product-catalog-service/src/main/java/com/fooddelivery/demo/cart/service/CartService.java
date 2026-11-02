package com.fooddelivery.demo.cart.service;

import com.fooddelivery.demo.cart.dto.*;

import java.util.UUID;

public interface CartService {

    CartResponse createOrGetCart(UUID customerId);

    CartResponse getCurrentCart(UUID customerId);

    CartResponse addItem(
            UUID customerId,
            AddCartItemRequest request
    );

    CartResponse updateItem(
            UUID customerId,
            UUID itemId,
            UpdateCartItemRequest request
    );

    void removeItem(UUID customerId, UUID itemId);

    void clearCart(UUID customerId);
}
