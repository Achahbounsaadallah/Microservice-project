package com.fooddelivery.demo.exception;

public class CartRestaurantMismatchException extends RuntimeException {
    public CartRestaurantMismatchException(String message) {
        super(message);
    }
}
