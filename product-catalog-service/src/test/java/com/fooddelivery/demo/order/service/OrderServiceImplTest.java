package com.fooddelivery.demo.order.service;

import com.fooddelivery.demo.cart.entity.Cart;
import com.fooddelivery.demo.cart.entity.CartItem;
import com.fooddelivery.demo.cart.entity.CartStatus;
import com.fooddelivery.demo.cart.repository.CartRepository;
import com.fooddelivery.demo.client.catalog.CatalogClient;
import com.fooddelivery.demo.client.catalog.CatalogRestaurantResponse;
import com.fooddelivery.demo.client.catalog.CatalogVariantResponse;
import com.fooddelivery.demo.exception.CartEmptyException;
import com.fooddelivery.demo.exception.CatalogNotFoundException;
import com.fooddelivery.demo.exception.CatalogUnavailableException;
import com.fooddelivery.demo.exception.CartNotFoundException;
import com.fooddelivery.demo.exception.InvalidOrderStatusException;
import com.fooddelivery.demo.exception.MealUnavailableException;
import com.fooddelivery.demo.exception.OrderNotFoundException;
import com.fooddelivery.demo.order.dto.CheckoutRequest;
import com.fooddelivery.demo.order.dto.OrderResponse;
import com.fooddelivery.demo.order.entity.Order;
import com.fooddelivery.demo.order.entity.OrderStatus;
import com.fooddelivery.demo.order.repository.OrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID RESTAURANT_A = UUID.randomUUID();
    private static final UUID VARIANT_PIZZA = UUID.randomUUID();
    private static final UUID MEAL_PIZZA = UUID.randomUUID();
    private static final UUID VARIANT_BURGER = UUID.randomUUID();
    private static final UUID MEAL_BURGER = UUID.randomUUID();

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CatalogClient catalogClient;

    private OrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(
                orderRepository,
                cartRepository,
                catalogClient,
                new BigDecimal("10.00")
        );
    }

    private Cart cartWithItems() {
        Cart cart = new Cart(CUSTOMER_ID, RESTAURANT_A);
        cart.addItem(new CartItem(VARIANT_PIZZA, 2));
        cart.addItem(new CartItem(VARIANT_BURGER, 1));
        return cart;
    }

    private CatalogVariantResponse pizza() {
        return new CatalogVariantResponse(
                VARIANT_PIZZA, MEAL_PIZZA, RESTAURANT_A,
                "Pizza", "medium", new BigDecimal("5.00"), true);
    }

    private CatalogVariantResponse burger() {
        return new CatalogVariantResponse(
                VARIANT_BURGER, MEAL_BURGER, RESTAURANT_A,
                "Burger", "small", new BigDecimal("10.00"), true);
    }

    private CheckoutRequest checkoutRequest() {
        return new CheckoutRequest("Rue 12, Casablanca", "+212600000000");
    }

    private Order storedOrder(OrderStatus status) {
        Cart cart = new Cart(CUSTOMER_ID, RESTAURANT_A);
        Order order = new Order(
                cart,
                CUSTOMER_ID,
                new BigDecimal("20.00"),
                new BigDecimal("10.00"),
                new BigDecimal("30.00"),
                "Rue 12",
                "+212600000000",
                RESTAURANT_A,
                "Restaurant A"
        );
        order.addItem(new com.fooddelivery.demo.order.entity.OrderItem(
                MEAL_PIZZA, VARIANT_PIZZA, "Pizza", "medium",
                new BigDecimal("5.00"), 2, new BigDecimal("10.00")));
        order.changeStatus(status);
        return order;
    }

    @Test
    void checkout_success_storesOfficialPricesAndCorrectTotals() {

        Cart cart = cartWithItems();
        when(cartRepository.findByCustomerIdAndStatus(CUSTOMER_ID, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(catalogClient.getVariant(VARIANT_PIZZA)).thenReturn(pizza());
        when(catalogClient.getVariant(VARIANT_BURGER)).thenReturn(burger());
        when(catalogClient.getRestaurant(RESTAURANT_A))
                .thenReturn(new CatalogRestaurantResponse(RESTAURANT_A, "Restaurant A", true));
        when(orderRepository.saveAndFlush(any(Order.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = service.checkout(CUSTOMER_ID, checkoutRequest());

        // subtotal = 2 * 5.00 + 10.00 = 20.00 ; total = 20.00 + 10.00 fee = 30.00
        assertEquals(new BigDecimal("20.00"), response.subtotal());
        assertEquals(new BigDecimal("10.00"), response.deliveryFee());
        assertEquals(new BigDecimal("30.00"), response.totalAmount());
        assertEquals(2, response.items().size());
        assertEquals(OrderStatus.PENDING, response.status());
        assertEquals("Pizza", response.items().get(0).mealName());
        assertEquals("medium", response.items().get(0).variantName());
        assertEquals(new BigDecimal("5.00"), response.items().get(0).unitPrice());
        assertEquals(new BigDecimal("10.00"), response.items().get(0).lineTotal());
        // Snapshot: prices come from Catalog, never from the client
        verify(catalogClient).getVariant(VARIANT_PIZZA);
        verify(catalogClient).getVariant(VARIANT_BURGER);
        verify(catalogClient).getRestaurant(RESTAURANT_A);
        // Cart converted after successful checkout
        assertEquals(CartStatus.CONVERTED, cart.getStatus());
        verify(cartRepository).save(cart);
    }

    @Test
    void checkout_noActiveCart_throwsCartNotFound() {

        when(cartRepository.findByCustomerIdAndStatus(CUSTOMER_ID, CartStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class,
                () -> service.checkout(CUSTOMER_ID, checkoutRequest()));

        verify(orderRepository, never()).saveAndFlush(any(Order.class));
    }

    @Test
    void checkout_emptyCart_throwsCartEmpty() {

        Cart emptyCart = new Cart(CUSTOMER_ID, RESTAURANT_A);
        when(cartRepository.findByCustomerIdAndStatus(CUSTOMER_ID, CartStatus.ACTIVE))
                .thenReturn(Optional.of(emptyCart));

        assertThrows(CartEmptyException.class,
                () -> service.checkout(CUSTOMER_ID, checkoutRequest()));

        verify(orderRepository, never()).saveAndFlush(any(Order.class));
    }

    @Test
    void checkout_unavailableVariant_rejectsAndPersistsNothing() {

        Cart cart = cartWithItems();
        when(cartRepository.findByCustomerIdAndStatus(CUSTOMER_ID, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(catalogClient.getVariant(VARIANT_PIZZA))
                .thenReturn(new CatalogVariantResponse(
                        VARIANT_PIZZA, MEAL_PIZZA, RESTAURANT_A,
                        "Pizza", "medium", new BigDecimal("5.00"), false));

        assertThrows(MealUnavailableException.class,
                () -> service.checkout(CUSTOMER_ID, checkoutRequest()));

        verify(orderRepository, never()).saveAndFlush(any(Order.class));
    }


    @Test
    void checkout_inactiveRestaurant_rejectsAndPersistsNothing() {

        Cart cart = cartWithItems();
        when(cartRepository.findByCustomerIdAndStatus(CUSTOMER_ID, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(catalogClient.getVariant(any(UUID.class))).thenReturn(pizza());
        when(catalogClient.getRestaurant(RESTAURANT_A))
                .thenReturn(new CatalogRestaurantResponse(RESTAURANT_A, "Closed", false));

        assertThrows(MealUnavailableException.class,
                () -> service.checkout(CUSTOMER_ID, checkoutRequest()));

        verify(orderRepository, never()).saveAndFlush(any(Order.class));
    }

    @Test
    void checkout_catalogDown_propagatesAndPersistsNothing() {

        Cart cart = cartWithItems();
        when(cartRepository.findByCustomerIdAndStatus(CUSTOMER_ID, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(catalogClient.getVariant(any(UUID.class)))
                .thenThrow(new CatalogUnavailableException("Unable to communicate"));

        assertThrows(CatalogUnavailableException.class,
                () -> service.checkout(CUSTOMER_ID, checkoutRequest()));

        verify(orderRepository, never()).saveAndFlush(any(Order.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void checkout_missingRestaurant_persistsNothing() {

        Cart cart = cartWithItems();
        when(cartRepository.findByCustomerIdAndStatus(CUSTOMER_ID, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(catalogClient.getVariant(any(UUID.class))).thenReturn(pizza());
        when(catalogClient.getVariant(VARIANT_BURGER)).thenReturn(burger());
        when(catalogClient.getRestaurant(RESTAURANT_A))
                .thenThrow(new CatalogNotFoundException("Restaurant not found"));

        assertThrows(CatalogNotFoundException.class,
                () -> service.checkout(CUSTOMER_ID, checkoutRequest()));

        verify(orderRepository, never()).saveAndFlush(any(Order.class));
    }

    @Test
    void getOrder_notFound_throwsOrderNotFound() {

        when(orderRepository.findWithItemsByIdAndCustomerId(any(), any()))
                .thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> service.getOrder(CUSTOMER_ID, UUID.randomUUID()));
    }

    @Test
    void getCustomerOrders_returnsHistory() {

        Order first = storedOrder(OrderStatus.CANCELLED);
        when(orderRepository.findAllByCustomerIdOrderByCreatedAtDesc(CUSTOMER_ID))
                .thenReturn(List.of(first));

        List<OrderResponse> history = service.getCustomerOrders(CUSTOMER_ID);

        assertEquals(1, history.size());
        assertEquals(OrderStatus.CANCELLED, history.get(0).status());
    }

    @Test
    void cancelOrder_pending_isCancelled() {

        Order order = storedOrder(OrderStatus.PENDING);
        when(orderRepository.findWithItemsByIdAndCustomerId(order.getId(), CUSTOMER_ID))
                .thenReturn(Optional.of(order));

        OrderResponse response = service.cancelOrder(CUSTOMER_ID, order.getId());

        assertEquals(OrderStatus.CANCELLED, response.status());
    }

    @Test
    void cancelOrder_confirmed_isCancelled() {

        Order order = storedOrder(OrderStatus.CONFIRMED);
        when(orderRepository.findWithItemsByIdAndCustomerId(order.getId(), CUSTOMER_ID))
                .thenReturn(Optional.of(order));

        OrderResponse response = service.cancelOrder(CUSTOMER_ID, order.getId());

        assertEquals(OrderStatus.CANCELLED, response.status());
    }

    @Test
    void cancelOrder_preparing_isRejected() {

        Order order = storedOrder(OrderStatus.PREPARING);
        when(orderRepository.findWithItemsByIdAndCustomerId(order.getId(), CUSTOMER_ID))
                .thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusException.class,
                () -> service.cancelOrder(CUSTOMER_ID, order.getId()));

        assertEquals(OrderStatus.PREPARING, order.getStatus());
    }

    @Test
    void cancelOrder_completed_isRejected() {

        Order order = storedOrder(OrderStatus.COMPLETED);
        when(orderRepository.findWithItemsByIdAndCustomerId(order.getId(), CUSTOMER_ID))
                .thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusException.class,
                () -> service.cancelOrder(CUSTOMER_ID, order.getId()));
    }

    @Test
    void changeStatus_pendingToConfirmed_isAllowed() {

        Order order = storedOrder(OrderStatus.PENDING);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        OrderResponse response = service.changeStatus(order.getId(), OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, response.status());
    }

    @Test
    void changeStatus_completedToPreparing_isRejected() {

        Order order = storedOrder(OrderStatus.COMPLETED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusException.class,
                () -> service.changeStatus(order.getId(), OrderStatus.PREPARING));
    }

    @Test
    void changeStatus_cancelledToConfirmed_isRejected() {

        Order order = storedOrder(OrderStatus.CANCELLED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusException.class,
                () -> service.changeStatus(order.getId(), OrderStatus.CONFIRMED));
    }

    @Test
    void changeStatus_orderNotFound_throws() {

        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> service.changeStatus(UUID.randomUUID(), OrderStatus.CONFIRMED));
    }
}
