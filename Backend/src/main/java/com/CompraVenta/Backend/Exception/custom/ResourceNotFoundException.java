package com.CompraVenta.Backend.Exception.custom;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    public ResourceNotFoundException(String resource, Object  id) {
        super(String.format("Recurso '%s' no encontrado con id: %s",resource,id));
    }
}
