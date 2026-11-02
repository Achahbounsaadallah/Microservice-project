package com.fooddelivery.demo.cart.service;

import com.fooddelivery.demo.cart.dto.*;
import com.fooddelivery.demo.cart.entity.*;
import com.fooddelivery.demo.cart.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartServiceImpl(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    // Create or get active cart
    @Override
    public CartResponse createOrGetCart(UUID customerId) {

        Cart cart = findOrCreateActiveCart(customerId);

        return mapToResponse(cart);
    }

    // Get current cart
    @Override
    @Transactional(readOnly = true)
    public CartResponse getCurrentCart(UUID customerId) {

        Cart cart = cartRepository
                .findByCustomerIdAndStatus(
                        customerId,
                        CartStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new RuntimeException("Active cart not found")
                );

        return mapToResponse(cart);
    }

    // Add item to cart
    @Override
    public CartResponse addItem(
            UUID customerId,
            AddCartItemRequest request
    ) {

        Cart cart = findOrCreateActiveCart(customerId);

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

            cart.addItem(newItem);
        }
        // Save and synchronize changes with PostgreSQL
        cartRepository.saveAndFlush(cart);

        return mapToResponse(cart);
    }

    // Update item quantity
    @Override
    public CartResponse updateItem(
            UUID customerId,
            UUID itemId,
            UpdateCartItemRequest request
    ) {

        Cart cart = findActiveCart(customerId);

        CartItem item = cartItemRepository
                .findByIdAndCart_Id(itemId, cart.getId())
                .orElseThrow(() ->
                        new RuntimeException("Cart item not found")
                );

        item.setQuantity(request.quantity());

        return mapToResponse(cart);
    }

    // Remove one item
    @Override
    public void removeItem(UUID customerId, UUID itemId) {

        Cart cart = findActiveCart(customerId);

        CartItem item = cartItemRepository
                .findByIdAndCart_Id(itemId, cart.getId())
                .orElseThrow(() ->
                        new RuntimeException("Cart item not found")
                );

        cart.removeItem(item);
    }

    // Clear the cart
    @Override
    public void clearCart(UUID customerId) {

        Cart cart = findActiveCart(customerId);

        cart.clearItems();
    }

    // Find active cart or create a new one
    private Cart findOrCreateActiveCart(UUID customerId) {

        return cartRepository
                .findByCustomerIdAndStatus(
                        customerId,
                        CartStatus.ACTIVE
                )
                .orElseGet(() ->
                        cartRepository.save(new Cart(customerId))
                );
    }

    // Find existing active cart
    private Cart findActiveCart(UUID customerId) {

        return cartRepository
                .findByCustomerIdAndStatus(
                        customerId,
                        CartStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new RuntimeException("Active cart not found")
                );
    }

    // Convert entity to DTO
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
                cart.getStatus(),
                items,
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }
}
