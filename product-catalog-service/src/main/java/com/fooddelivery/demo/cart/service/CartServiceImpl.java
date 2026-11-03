
package com.fooddelivery.demo.cart.service;

import com.fooddelivery.demo.cart.dto.AddCartItemRequest;
import com.fooddelivery.demo.cart.dto.CartItemResponse;
import com.fooddelivery.demo.cart.dto.CartResponse;
import com.fooddelivery.demo.cart.dto.UpdateCartItemRequest;

import com.fooddelivery.demo.cart.entity.Cart;
import com.fooddelivery.demo.cart.entity.CartItem;
import com.fooddelivery.demo.cart.entity.CartStatus;

import com.fooddelivery.demo.cart.repository.CartRepository;
import com.fooddelivery.demo.cart.repository.CartItemRepository;

import com.fooddelivery.demo.client.catalog.CatalogClient;
import com.fooddelivery.demo.client.catalog.CatalogRestaurantResponse;
import com.fooddelivery.demo.client.catalog.CatalogVariantResponse;

import com.fooddelivery.demo.exception.CartNotFoundException;
import com.fooddelivery.demo.exception.CartItemNotFoundException;
import com.fooddelivery.demo.exception.CartRestaurantMismatchException;
import com.fooddelivery.demo.exception.MealUnavailableException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CatalogClient catalogClient;

    // Constructor injection
    public CartServiceImpl(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            CatalogClient catalogClient
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.catalogClient = catalogClient;
    }

    // ==========================================
    // 1. CREATE OR GET CART
    // ==========================================

    @Override
    public CartResponse createOrGetCart(
            UUID customerId,
            UUID restaurantId
    ) {

        // Verify the restaurant exists and is open in Catalog
        CatalogRestaurantResponse restaurant =
                catalogClient.getRestaurant(restaurantId);

        if (!restaurant.active()) {

            throw new MealUnavailableException(
                    "This restaurant is not accepting orders at the moment"
            );
        }

        // Find the customer's active cart
        Cart cart = cartRepository
                .findByCustomerIdAndStatus(
                        customerId,
                        CartStatus.ACTIVE
                )
                .orElse(null);

        // If no active cart exists, create one
        if (cart == null) {

            cart = new Cart(customerId, restaurantId);

            cart = cartRepository.saveAndFlush(cart);

        } else {

            // Check if the selected restaurant is different
            if (!Objects.equals(
                    cart.getRestaurantId(),
                    restaurantId
            )) {

                // Do not allow switching with existing items
                if (!cart.getItems().isEmpty()) {

                    throw new CartRestaurantMismatchException(
                            "Your cart contains items from another restaurant. Clear it before switching."
                    );
                }

                // Change restaurant if cart is empty
                cart.changeRestaurant(restaurantId);

                cartRepository.saveAndFlush(cart);
            }
        }

        return mapToResponse(cart);
    }

    // ==========================================
    // 2. GET CURRENT CART
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCurrentCart(UUID customerId) {

        Cart cart = findActiveCart(customerId);

        return mapToResponse(cart);
    }

    // ==========================================
    // 3. ADD ITEM TO CART
    // ==========================================

    @Override
    public CartResponse addItem(
            UUID customerId,
            AddCartItemRequest request
    ) {

        // Retrieve the existing active cart
        Cart cart = findActiveCart(customerId);

        // Retrieve variant information from Catalog
        CatalogVariantResponse variant =
                catalogClient.getVariant(request.variantId());

        // Check meal availability
        if (!variant.available()) {

            throw new MealUnavailableException(
                    "This meal is currently unavailable"
            );
        }

        // Check that the variant belongs to the selected restaurant
        if (!Objects.equals(
                variant.restaurantId(),
                cart.getRestaurantId()
        )) {

            throw new CartRestaurantMismatchException(
                    "This meal does not belong to the selected restaurant"
            );
        }

        // Check whether this variant already exists in the cart
        var existingItem = cartItemRepository
                .findByCart_IdAndVariantId(
                        cart.getId(),
                        request.variantId()
                );

        if (existingItem.isPresent()) {

            // Increase existing quantity
            CartItem item = existingItem.get();

            item.setQuantity(
                    item.getQuantity() + request.quantity()
            );

        } else {

            // Create a new cart item
            CartItem newItem = new CartItem(
                    request.variantId(),
                    request.quantity()
            );

            // Maintain both sides of the JPA relationship
            cart.addItem(newItem);
        }

        // Persist changes and generate new item UUID
        cartRepository.saveAndFlush(cart);

        return mapToResponse(cart);
    }

    // ==========================================
    // 4. UPDATE ITEM QUANTITY
    // ==========================================

    @Override
    public CartResponse updateItem(
            UUID customerId,
            UUID itemId,
            UpdateCartItemRequest request
    ) {

        Cart cart = findActiveCart(customerId);

        CartItem item = cartItemRepository
                .findByIdAndCart_Id(
                        itemId,
                        cart.getId()
                )
                .orElseThrow(() ->
                        new CartItemNotFoundException(
                                "Cart item not found"
                        )
                );

        item.setQuantity(request.quantity());

        return mapToResponse(cart);
    }

    // ==========================================
    // 5. REMOVE ONE ITEM
    // ==========================================

    @Override
    public void removeItem(
            UUID customerId,
            UUID itemId
    ) {

        Cart cart = findActiveCart(customerId);

        CartItem item = cartItemRepository
                .findByIdAndCart_Id(
                        itemId,
                        cart.getId()
                )
                .orElseThrow(() ->
                        new CartItemNotFoundException(
                                "Cart item not found"
                        )
                );

        cart.removeItem(item);
    }

    // ==========================================
    // 6. CLEAR CART
    // ==========================================

    @Override
    public void clearCart(UUID customerId) {

        Cart cart = findActiveCart(customerId);

        cart.clearItems();
    }

    // ==========================================
    // 7. FIND ACTIVE CART
    // ==========================================

    private Cart findActiveCart(UUID customerId) {

        return cartRepository
                .findByCustomerIdAndStatus(
                        customerId,
                        CartStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Active cart not found"
                        )
                );
    }

    // ==========================================
    // 8. ENTITY TO DTO MAPPING
    // ==========================================

    private CartResponse mapToResponse(Cart cart) {

        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        item.getVariantId(),
                        item.getQuantity()
                ))
                .toList();

        return new CartResponse(
                cart.getId(),
                cart.getRestaurantId(),
                cart.getStatus(),
                items,
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }
}
