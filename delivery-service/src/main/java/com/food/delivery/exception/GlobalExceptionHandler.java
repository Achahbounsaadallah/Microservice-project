
package com.food.delivery.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> gererErreur(
            IllegalArgumentException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }



    @ExceptionHandler(LivraisonIntrouvableException.class)
public ResponseEntity<Map<String, String>> gererLivraisonIntrouvable(
        LivraisonIntrouvableException ex) {

    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(Map.of("message", ex.getMessage()));
}

@ExceptionHandler(IllegalStateException.class)
public ResponseEntity<Map<String, String>> gererConflit(
        IllegalStateException ex) {

    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(Map.of("message", ex.getMessage()));
}
}
