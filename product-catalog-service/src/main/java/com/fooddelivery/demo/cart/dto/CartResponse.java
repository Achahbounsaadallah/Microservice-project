package com.fooddelivery.demo.cart.dto;

import com.fooddelivery.demo.cart.entity.CartStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CartResponse(

        UUID id,

        CartStatus status,

        List<CartItemResponse> items,

        Instant createdAt,

        Instant updatedAt

) {}