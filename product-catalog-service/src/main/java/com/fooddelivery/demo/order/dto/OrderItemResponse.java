package com.fooddelivery.demo.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(

        UUID id,

        UUID mealId,

        UUID variantId,

        String mealName,

        String variantName,

        BigDecimal unitPrice,

        Integer quantity,

        BigDecimal lineTotal

) {}
