package com.fooddelivery.demo.order.controller;

import com.fooddelivery.demo.order.dto.*;
import com.fooddelivery.demo.order.entity.OrderStatus;
import com.fooddelivery.demo.order.service.OrderService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ==================================
    // CHECKOUT
    // ==================================

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(

            @RequestHeader("X-Customer-Id") UUID customerId,

            @Valid @RequestBody CheckoutRequest request

    ) {

        OrderResponse response =
                orderService.checkout(customerId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // ==================================
    // GET CUSTOMER ORDER HISTORY
    // ==================================

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getCustomerOrders(

            @RequestHeader("X-Customer-Id") UUID customerId

    ) {

        return ResponseEntity.ok(
                orderService.getCustomerOrders(customerId)
        );
    }

    // ==================================
    // GET ORDER BY ID
    // ==================================

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(

            @RequestHeader("X-Customer-Id") UUID customerId,

            @PathVariable UUID orderId

    ) {

        return ResponseEntity.ok(
                orderService.getOrder(customerId, orderId)
        );
    }

    // ==================================
    // CANCEL ORDER
    // ==================================

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(

            @RequestHeader("X-Customer-Id") UUID customerId,

            @PathVariable UUID orderId

    ) {

        return ResponseEntity.ok(
                orderService.cancelOrder(customerId, orderId)
        );
    }

    // ==================================
    // CHANGE ORDER STATUS
    // ==================================

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> changeStatus(

            @PathVariable UUID orderId,

            @Valid @RequestBody ChangeOrderStatusRequest request

    ) {

        return ResponseEntity.ok(
                orderService.changeStatus(
                        orderId,
                        request.status()
                )
        );
    }
}
