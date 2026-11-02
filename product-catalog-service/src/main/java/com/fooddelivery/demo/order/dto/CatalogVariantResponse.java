package com.fooddelivery.demo.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CatalogVariantResponse(

        UUID id,

        UUID mealId,

        String mealName,

        String size,

        BigDecimal price,

        boolean available

) {}