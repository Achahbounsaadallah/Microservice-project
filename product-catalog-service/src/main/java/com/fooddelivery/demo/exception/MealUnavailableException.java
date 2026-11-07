package com.fooddelivery.demo.exception;

public class MealUnavailableException extends RuntimeException {

    public MealUnavailableException(String message) {
        super(message);
    }
}