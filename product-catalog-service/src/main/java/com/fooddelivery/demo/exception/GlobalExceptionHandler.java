package com.fooddelivery.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @ExceptionHandler(CartItemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleCartItemNotFound(
            CartItemNotFoundException ex
    ) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(CartRestaurantMismatchException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleCartRestaurantMismatch(
            CartRestaurantMismatchException ex
    ) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(CatalogNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleCatalogNotFound(
            CatalogNotFoundException ex
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationErrors(
            MethodArgumentNotValidException ex
    ) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return Map.of(
                "error", "Validation failed",
                "details", errors
        );
    }
}
