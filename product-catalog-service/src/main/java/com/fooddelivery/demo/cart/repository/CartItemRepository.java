package com.fooddelivery.demo.cart.repository;

import com.fooddelivery.demo.cart.entity.CartItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository
        extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCart_IdAndVariantId(
            UUID cartId,
            UUID variantId
    );

    Optional<CartItem> findByIdAndCart_Id(
            UUID itemId,
            UUID cartId
    );
}
