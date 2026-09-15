package com.CompraVenta.Backend.Sync.client;

public class SyncConflictException extends RuntimeException {
    public SyncConflictException(String message) {
        super(message);
    }
}
