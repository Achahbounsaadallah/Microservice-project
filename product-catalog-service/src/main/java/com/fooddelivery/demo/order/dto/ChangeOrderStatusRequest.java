package com.fooddelivery.demo.order.dto;

import com.fooddelivery.demo.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeOrderStatusRequest(

        @NotNull
        OrderStatus status

) {}