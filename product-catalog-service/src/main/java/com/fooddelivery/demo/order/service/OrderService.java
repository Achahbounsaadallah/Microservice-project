package com.fooddelivery.demo.order.service;
import com.fooddelivery.demo.order.dto.CheckoutRequest;
import com.fooddelivery.demo.order.dto.OrderResponse;
import com.fooddelivery.demo.order.entity.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderResponse checkout(
            UUID customerId,
            CheckoutRequest request
    );

    OrderResponse getOrder(
            UUID customerId,
            UUID orderId
    );

    List<OrderResponse> getCustomerOrders(UUID customerId);

    OrderResponse cancelOrder(
            UUID customerId,
            UUID orderId
    );

    OrderResponse changeStatus(
            UUID orderId,
            OrderStatus newStatus
    );
}