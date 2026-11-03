package com.fooddelivery.demo.exception;

public class CatalogNotFoundException extends RuntimeException {

    public CatalogNotFoundException(String message) {
        super(message);
    }
}
