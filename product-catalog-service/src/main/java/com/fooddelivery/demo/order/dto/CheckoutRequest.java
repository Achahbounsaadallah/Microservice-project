package com.fooddelivery.demo.order.dto;


import jakarta.validation.constraints.NotBlank;

public record CheckoutRequest(

        @NotBlank(message = "Delivery address is required")
        String deliveryAddress,

        @NotBlank(message = "Customer phone is required")
        String customerPhone

) {}