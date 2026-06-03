package com.CompraVenta.Backend.Exception.custom;

public class UnauthorizedException extends RuntimeException{
    public UnauthorizedException(String message){
        super(message);
    }
    public UnauthorizedException(String action, String requiredRole){
        super(String.format("Unauthorized access to action %s with required role %s", action, requiredRole));
    }
}
