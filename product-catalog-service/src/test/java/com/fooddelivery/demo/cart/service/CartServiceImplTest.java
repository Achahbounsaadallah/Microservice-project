package com.fooddelivery.demo.cart.service;

import com.fooddelivery.demo.cart.dto.AddCartItemRequest;
import com.fooddelivery.demo.cart.dto.CartResponse;
import com.fooddelivery.demo.cart.entity.Cart;
import com.fooddelivery.demo.cart.entity.CartItem;
import com.fooddelivery.demo.cart.entity.CartStatus;
import com.fooddelivery.demo.cart.repository.CartItemRepository;
import com.fooddelivery.demo.cart.repository.CartRepository;
import com.fooddelivery.demo.client.catalog.CatalogClient;
import com.fooddelivery.demo.client.catalog.CatalogRestaurantResponse;
import com.fooddelivery.demo.client.catalog.CatalogVariantResponse;
import com.fooddelivery.demo.exception.CartItemNotFoundException;
import com.fooddelivery.demo.exception.CartNotFoundException;
import com.fooddelivery.demo.exception.CartRestaurantMismatchException;
import com.fooddelivery.demo.exception.MealUnavailableException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID RESTAURANT_A = UUID.randomUUID();
    private static final UUID RESTAURANT_B = UUID.randomUUID();
    private static final UUID VARIANT_PIZZA = UUID.randomUUID();
    private static final UUID MEAL_PIZZA = UUID.randomUUID();

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CatalogClient catalogClient;

    @InjectMocks
    private CartServiceImpl service;

    private Cart activeCart(UUID restaurantId) {
        return new Cart(CUSTOMER_ID, restaurantId);
    }

    private CatalogRestaurantResponse openRestaurant(UUID id) {
        return new CatalogRestaurantResponse(id, "Restaurant " + id, true);
    }

    @Test
    void createOrGetCart_noExistingCart_createsCartForSelectedRestaurant() {

        when(catalogClient.getRestaurant(RESTAURANT_A))
                .thenReturn(openRestaurant(RESTAURANT_A));
        when(cartRepository.findByCustomerIdAndStatus(CUSTOMER_ID, CartStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(cartRepository.saveAndFlush(any(Cart.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CartResponse response = service.createOrGetCart(CUSTOMER_ID, RESTAURANT_A);

        assertEquals(RESTAURANT_A, response.restaurantId());
        assertEquals(CartStatus.ACTIVE, response.status());
        assertTrue(response.items().isEmpty());
    }

    @Test
    void createOrGetCart_emptyExistingCart_switchesRestaurant() {

        when(catalogClient.getRestaurant(RESTAURANT_A))
                .thenReturn(openRestaurant(RESTAURANT_A));
        Cart existing = activeCart(RESTAURANT_B);
        when(cartRepository.findByCustomerIdAndStatus(CUSTOMER_ID, CartStatus.ACTIVE))
                .thenReturn(Optional.of(existing));
        when(cartRepository.saveAndFlush(any(Cart.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CartResponse response = service.createOrGetCart(CUSTOMER_ID, RESTAURANT_A);

        assertEquals(RESTAURANT_A, response.restaurantId());
        verify(cartRepository, times(1)).saveAndFlush(existing);
    }

    @Test
    void createOrGetCart_cartWithItemsFromOtherRestaurant_isRejected() {

        when(catalogClient.getRestaurant(RESTAURANT_A))
                .thenReturn(openRestaurant(RESTAURANT_A));
        Cart existing = new Cart(CUSTOMER_ID, RESTAURANT_B);
        existing.addItem(new CartItem(VARIANT_PIZZA, 1));
        when(cartRepository.findByCustomerIdAndStatus(CUSTOMER_ID, CartStatus.ACTIVE))
                .thenReturn(Optional.of(existing));

        assertThrows(CartRestaurantMismatchException.class,
                () -> service.createOrGetCart(CUSTOMER_ID, RESTAURANT_A));

        assertEquals(RESTAURANT_B, existing.getRestaurantId());
        verify(cartRepository, never()).saveAndFlush(any(Cart.class));
    }

    @Test
    void createOrGetCart_inactiveRestaurant_isRejected() {

        when(catalogClient.getRestaurant(RESTAURANT_A))
                .thenReturn(new CatalogRestaurantResponse(RESTAURANT_A, "Closed", false));

        assertThrows(MealUnavailableException.class,
                () -> service.createOrGetCart(CUSTOMER_ID, RESTAURANT_A));

        verify(cartRepository, never()).saveAndFlush(any(Cart.class));
    }

    @Test
    void addItem_newVariant_addsItemToCart() {

        Cart cart = activeCart(RESTAURANT_A);
        when(cartRepository.findByCustomerIdAndStatus(CUSTOMER_ID, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(catalogClient.getVariant(VARIANT_PIZZA))
                .thenReturn(new CatalogVariantResponse(
                        VARIANT_PIZZA, MEAL_PIZZA, RESTAURANT_A,
                        "Pizza", "medium", new BigDecimal("9.00"), true));
        when(cartItemRepository.findByCart_IdAndVariantId(cart.getId(), VARIANT_PIZZA))
                .thenReturn(Optional.empty());
        when(cartRepository.saveAndFlush(any(Cart.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CartResponse response = service.addItem(
                CUSTOMER_ID, new AddCartItemRequest(VARIANT_PIZZA, 2));

        assertEquals(1, response.items().size());
        assertEquals(2, response.items().get(0).quantity());
        assertEquals(VARIANT_PIZZA, response.items().get(0).variantId());
    }

    @Test
    void addItem_variantAlreadyInCart_incrementsQuantity() {

        Cart cart = activeCart(RESTAURANT_A);
        cart.addItem(new CartItem(VARIANT_PIZZA, 1));
        when(cartRepository.findByCustomerIdAndStatus(CUSTOMER_ID, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(catalogClient.getVariant(VARIANT_PIZZA))
                .thenReturn(new CatalogVariantResponse(
                        VARIANT_PIZZA, MEAL_PIZZA, RESTAURANT_A,
                        "Pizza", "medium", new BigDecimal("9.00"), true));
        when(cartItemRepository.findByCart_IdAndVariantId(cart.getId(), VARIANT_PIZZA))
                .thenReturn(Optional.of(cart.getItems().get(0)));
        when(cartRepository.saveAndFlush(any(Cart.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CartResponse response = service.addItem(
                CUSTOMER_ID, new AddCartItemRequest(VARIANT_PIZZA, 2));

        assertEquals(1, response.items().size());
        assertEquals(3, response.items().get(0).quantity());
    }

    @Test
    void addItem_unavailableVariant_isRejected() {

        Cart cart = activeCart(RESTAURANT_A);
        when(cartRepository.findByCustomerIdAndStatus(CUSTOMER_ID, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(catalogClient.getVariant(VARIANT_PIZZA))
                .thenReturn(new CatalogVariantResponse(
                        VARIANT_PIZZA, MEAL_PIZZA, RESTAURANT_A,
                        "Pizza", "medium", new BigDecimal("9.00"), false));

        assertThrows(MealUnavailableException.class,
                () -> service.addItem(CUSTOMER_ID, new AddCartItemRequest(VARIANT_PIZZA, 1)));

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void addItem_variantOfAnotherRestaurant_isRejected() {

        Cart cart = activeCart(RESTAURANT_A);
        when(cartRepository.findByCustomerIdAndStatus(CUSTOMER_ID, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(catalogClient.getVariant(VARIANT_PIZZA))
                .thenReturn(new CatalogVariantResponse(
                        VARIANT_PIZZA, MEAL_PIZZA, RESTAURANT_B,
                        "Burger", "small", new BigDecimal("7.00"), true));

        assertThrows(CartRestaurantMismatchException.class,
                () -> service.addItem(CUSTOMER_ID, new AddCartItemRequest(VARIANT_PIZZA, 1)));
    }

    @Test
    void getCurrentCart_noActiveCart_throwsNotFound() {

        when(cartRepository.findByCustomerIdAndStatus(CUSTOMER_ID, CartStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class,
                () -> service.getCurrentCart(CUSTOMER_ID));
    }

    @Test
    void updateItem_notFoundInCart_throwsCartItemNotFound() {

        Cart cart = activeCart(RESTAURANT_A);
        UUID unknownItemId = UUID.randomUUID();
        when(cartRepository.findByCustomerIdAndStatus(CUSTOMER_ID, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(cartItemRepository.findByIdAndCart_Id(unknownItemId, cart.getId()))
                .thenReturn(Optional.empty());

        assertThrows(CartItemNotFoundException.class,
                () -> service.updateItem(CUSTOMER_ID, unknownItemId,
                        new com.fooddelivery.demo.cart.dto.UpdateCartItemRequest(2)));
    }

    @Test
    void updateItem_found_updatesQuantity() {

        Cart cart = activeCart(RESTAURANT_A);
        cart.addItem(new CartItem(VARIANT_PIZZA, 1));
        UUID itemId = cart.getItems().get(0).getId();
        when(cartRepository.findByCustomerIdAndStatus(CUSTOMER_ID, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(cartItemRepository.findByIdAndCart_Id(itemId, cart.getId()))
                .thenReturn(Optional.of(cart.getItems().get(0)));

        CartResponse response = service.updateItem(CUSTOMER_ID, itemId,
                new com.fooddelivery.demo.cart.dto.UpdateCartItemRequest(5));

        assertEquals(5, response.items().get(0).quantity());
    }

    @Test
    void removeItem_found_removesFromCart() {

        Cart cart = activeCart(RESTAURANT_A);
        cart.addItem(new CartItem(VARIANT_PIZZA, 1));
        UUID itemId = cart.getItems().get(0).getId();
        when(cartRepository.findByCustomerIdAndStatus(CUSTOMER_ID, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(cartItemRepository.findByIdAndCart_Id(itemId, cart.getId()))
                .thenReturn(Optional.of(cart.getItems().get(0)));

        service.removeItem(CUSTOMER_ID, itemId);

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void clearCart_removesAllItems() {

        Cart cart = activeCart(RESTAURANT_A);
        cart.addItem(new CartItem(VARIANT_PIZZA, 1));
        cart.addItem(new CartItem(UUID.randomUUID(), 2));
        when(cartRepository.findByCustomerIdAndStatus(CUSTOMER_ID, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));

        service.clearCart(CUSTOMER_ID);

        assertTrue(cart.getItems().isEmpty());
    }
}
