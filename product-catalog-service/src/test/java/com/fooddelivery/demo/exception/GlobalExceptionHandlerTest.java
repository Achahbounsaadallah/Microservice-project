package com.fooddelivery.demo.exception;

import com.fooddelivery.demo.cart.controller.CartController;
import com.fooddelivery.demo.cart.service.CartService;
import com.fooddelivery.demo.order.controller.OrderController;
import com.fooddelivery.demo.order.dto.CheckoutRequest;
import com.fooddelivery.demo.order.dto.OrderResponse;
import com.fooddelivery.demo.order.entity.OrderStatus;
import com.fooddelivery.demo.order.service.OrderService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = { CartController.class, OrderController.class })
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private OrderService orderService;

    private static final String CUSTOMER_HEADER = UUID.randomUUID().toString();

    @Test
    void validationError_returns400_withFieldDetails() throws Exception {

        mockMvc.perform(post("/api/v1/orders/checkout")
                        .header("X-Customer-Id", CUSTOMER_HEADER)
                        .contentType("application/json")
                        .content("{\"deliveryAddress\": \"\", \"customerPhone\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void missingQuantity_returns400() throws Exception {

        mockMvc.perform(post("/api/v1/carts/current/items")
                        .header("X-Customer-Id", CUSTOMER_HEADER)
                        .contentType("application/json")
                        .content("{\"variantId\": \"" + UUID.randomUUID() + "\", \"quantity\": 0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cartRestaurantMismatch_returns409() throws Exception {

        when(cartService.createOrGetCart(any(), any()))
                .thenThrow(new CartRestaurantMismatchException("Your cart contains items from another restaurant"));

        mockMvc.perform(post("/api/v1/carts/current")
                        .header("X-Customer-Id", CUSTOMER_HEADER)
                        .contentType("application/json")
                        .content("{\"restaurantId\": \"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void cartItemNotFound_returns404() throws Exception {

        doThrow(new CartItemNotFoundException("Cart item not found"))
                .when(cartService).removeItem(any(), any());

        mockMvc.perform(delete("/api/v1/carts/current/items/" + UUID.randomUUID())
                        .header("X-Customer-Id", CUSTOMER_HEADER))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Cart item not found"));
    }

    @Test
    void cartNotFound_returns404() throws Exception {

        when(cartService.getCurrentCart(any()))
                .thenThrow(new CartNotFoundException("Active cart not found"));

        mockMvc.perform(get("/api/v1/carts/current")
                        .header("X-Customer-Id", CUSTOMER_HEADER))
                .andExpect(status().isNotFound());
    }

    @Test
    void orderNotFound_returns404() throws Exception {

        when(orderService.cancelOrder(any(), any()))
                .thenThrow(new OrderNotFoundException("Order not found"));

        mockMvc.perform(post("/api/v1/orders/" + UUID.randomUUID() + "/cancel")
                        .header("X-Customer-Id", CUSTOMER_HEADER))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidOrderStatus_returns409() throws Exception {

        when(orderService.changeStatus(any(), any()))
                .thenThrow(new InvalidOrderStatusException("Invalid transition"));

        mockMvc.perform(patch("/api/v1/orders/" + UUID.randomUUID() + "/status")
                        .contentType("application/json")
                        .content("{\"status\": \"PENDING\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidStatusPayload_returns400() throws Exception {

        mockMvc.perform(patch("/api/v1/orders/" + UUID.randomUUID() + "/status")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void catalogUnavailable_returns503() throws Exception {

        when(orderService.checkout(any(), any()))
                .thenThrow(new CatalogUnavailableException("Unable to communicate with Catalog Service"));

        mockMvc.perform(post("/api/v1/orders/checkout")
                        .header("X-Customer-Id", CUSTOMER_HEADER)
                        .contentType("application/json")
                        .content("{\"deliveryAddress\": \"Rue 12\", \"customerPhone\": \"0600\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Unable to communicate with Catalog Service"));
    }

    @Test
    void checkoutSuccess_returns201() throws Exception {

        UUID customerId = UUID.randomUUID();
        when(orderService.checkout(any(), any()))
                .thenReturn(new OrderResponse(
                        UUID.randomUUID(), "ORD-12345678", customerId,
                        OrderStatus.PENDING, List.of(),
                        new BigDecimal("20.00"), new BigDecimal("10.00"),
                        new BigDecimal("30.00"), "Rue 12", "0600",
                        null, null));

        mockMvc.perform(post("/api/v1/orders/checkout")
                        .header("X-Customer-Id", CUSTOMER_HEADER)
                        .contentType("application/json")
                        .content("{\"deliveryAddress\": \"Rue 12\", \"customerPhone\": \"0600\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("ORD-12345678"))
                .andExpect(jsonPath("$.subtotal").value(20.00))
                .andExpect(jsonPath("$.totalAmount").value(30.00));
    }

    @Test
    void cartResponse_isMappedWithoutJpaEntities() throws Exception {

        UUID customerId = UUID.randomUUID();
        when(cartService.getCurrentCart(any())).thenReturn(
                new com.fooddelivery.demo.cart.dto.CartResponse(
                        UUID.randomUUID(), UUID.randomUUID(),
                        com.fooddelivery.demo.cart.entity.CartStatus.ACTIVE,
                        List.of(new com.fooddelivery.demo.cart.dto.CartItemResponse(
                                UUID.randomUUID(), UUID.randomUUID(), 2)),
                        null, null)
        );

        mockMvc.perform(get("/api/v1/carts/current")
                        .header("X-Customer-Id", CUSTOMER_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }
}
