package com.fooddelivery.demo.cart.controller;

import com.fooddelivery.demo.cart.dto.*;
import com.fooddelivery.demo.cart.service.CartService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Create or retrieve current cart
    @PostMapping("/current")
    public ResponseEntity<CartResponse> createOrGetCart(

            @RequestHeader("X-Customer-Id") UUID customerId,

            @Valid @RequestBody CreateCartRequest request

    ) {

        return ResponseEntity.ok(
                cartService.createOrGetCart(
                        customerId,
                        request.restaurantId()
                )
        );
    }

    // Get current cart
    @GetMapping("/current")
    public ResponseEntity<CartResponse> getCurrentCart(
            @RequestHeader("X-Customer-Id") UUID customerId
    ) {

        return ResponseEntity.ok(
                cartService.getCurrentCart(customerId)
        );
    }

    // Add an item
    @PostMapping("/current/items")
    public ResponseEntity<CartResponse> addItem(
            @RequestHeader("X-Customer-Id") UUID customerId,

            @Valid @RequestBody AddCartItemRequest request
    ) {

        return ResponseEntity.ok(
                cartService.addItem(customerId, request)
        );
    }

    // Update quantity
    @PatchMapping("/current/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            @RequestHeader("X-Customer-Id") UUID customerId,

            @PathVariable UUID itemId,

            @Valid @RequestBody UpdateCartItemRequest request
    ) {

        return ResponseEntity.ok(
                cartService.updateItem(customerId, itemId, request)
        );
    }

    // Remove an item
    @DeleteMapping("/current/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            @RequestHeader("X-Customer-Id") UUID customerId,

            @PathVariable UUID itemId
    ) {

        cartService.removeItem(customerId, itemId);

        return ResponseEntity.noContent().build();
    }

    // Clear cart
    @DeleteMapping("/current/items")
    public ResponseEntity<Void> clearCart(
            @RequestHeader("X-Customer-Id") UUID customerId
    ) {

        cartService.clearCart(customerId);

        return ResponseEntity.noContent().build();
    }
}
