
package com.fooddelivery.demo.cart.service;

import com.fooddelivery.demo.cart.dto.AddCartItemRequest;
import com.fooddelivery.demo.cart.dto.CartResponse;
import com.fooddelivery.demo.cart.dto.UpdateCartItemRequest;

import java.util.UUID;

public interface CartService {

    // Create or retrieve a cart for the selected restaurant
    CartResponse createOrGetCart(
            UUID customerId,
            UUID restaurantId
    );

    // Retrieve the customer's active cart
    CartResponse getCurrentCart(UUID customerId);

    // Add a meal variant to the active cart
    CartResponse addItem(
            UUID customerId,
            AddCartItemRequest request
    );

    // Update the quantity of a cart item
    CartResponse updateItem(
            UUID customerId,
            UUID itemId,
            UpdateCartItemRequest request
    );

    // Remove one item from the cart
    void removeItem(UUID customerId, UUID itemId);

    // Remove all items from the cart
    void clearCart(UUID customerId);
}
