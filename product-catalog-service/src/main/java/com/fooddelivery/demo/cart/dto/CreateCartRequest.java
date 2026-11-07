package com.fooddelivery.demo.cart.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record CreateCartRequest(
        @NotNull UUID restaurantId
) {}