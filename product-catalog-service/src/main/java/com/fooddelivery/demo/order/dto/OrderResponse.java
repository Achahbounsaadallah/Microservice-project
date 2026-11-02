package com.fooddelivery.demo.order.dto;

import com.fooddelivery.demo.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(

        UUID id,

        String orderNumber,

        UUID customerId,

        OrderStatus status,

        List<OrderItemResponse> items,

        BigDecimal subtotal,

        BigDecimal deliveryFee,

        BigDecimal totalAmount,

        String deliveryAddress,

        String customerPhone,

        Instant createdAt,

        Instant updatedAt

) {}