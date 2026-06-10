package com.CompraVenta.Backend.Exception.custom;

public class DuplicateResourceException extends RuntimeException {
    private final String existingGlobalId;

    public DuplicateResourceException(String message, String existingGlobalId) {
        super(message);
        this.existingGlobalId = existingGlobalId;
    }

    public String getExistingGlobalId() {
        return existingGlobalId;
    }
}
