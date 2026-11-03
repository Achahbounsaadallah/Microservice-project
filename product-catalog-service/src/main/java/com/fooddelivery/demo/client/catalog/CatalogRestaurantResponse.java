package com.fooddelivery.demo.client.catalog;

import java.util.UUID;

public record CatalogRestaurantResponse(
        UUID id,
        String name,
        boolean active
) {}
