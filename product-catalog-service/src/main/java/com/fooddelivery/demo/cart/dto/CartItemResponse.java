package com.fooddelivery.demo.cart.dto;

import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID variantId,
        Integer quantity
) {}
