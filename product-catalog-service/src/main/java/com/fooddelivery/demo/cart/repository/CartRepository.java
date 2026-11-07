package com.fooddelivery.demo.cart.repository;

import com.fooddelivery.demo.cart.entity.Cart;
import com.fooddelivery.demo.cart.entity.CartStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    @EntityGraph(attributePaths = "items")
    Optional<Cart> findByCustomerIdAndStatus(
            UUID customerId,
            CartStatus status
    );
}