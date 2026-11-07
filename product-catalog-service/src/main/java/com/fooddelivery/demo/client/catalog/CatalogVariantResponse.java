package com.fooddelivery.demo.client.catalog;

import java.math.BigDecimal;
import java.util.UUID;

public record CatalogVariantResponse(
        UUID id,
        UUID mealId,
        UUID restaurantId,
        String mealName,
        String size,
        BigDecimal price,
        boolean available
) {}
