package com.CompraVenta.Backend.Exception.custom;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    public ResourceNotFoundException(String resource, Object  id) {
        super(String.format("Recursis '%s' no enctrado con id: %s",resource,id));
    }
}
