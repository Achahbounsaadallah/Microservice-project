package com.food.delivery.exception;

public class LivraisonIntrouvableException
        extends RuntimeException {

    public LivraisonIntrouvableException(String message) {
        super(message);
    }
}