package com.fooddelivery.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CartEmptyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleEmptyCart(
            CartEmptyException ex
    ) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(CartNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleCartNotFound(
            CartNotFoundException ex
    ) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleOrderNotFound(
            OrderNotFoundException ex
    ) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(MealUnavailableException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleMealUnavailable(
            MealUnavailableException ex
    ) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(InvalidOrderStatusException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleInvalidStatus(
            InvalidOrderStatusException ex
    ) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(CatalogUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> handleCatalogUnavailable(
            CatalogUnavailableException ex
    ) {
        return Map.of("error", ex.getMessage());
    }
}
