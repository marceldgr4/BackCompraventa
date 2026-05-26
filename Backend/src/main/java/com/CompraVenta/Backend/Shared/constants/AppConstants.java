package com.CompraVenta.Backend.Shared.constants;

public class AppConstants {
    private AppConstants() {}

    public static final int MAX_SYNC_RETRIES   = 5;
    public static final int SYNC_BATCH_SIZE    = 100;
    public static final int BCRYPT_STRENGTH    = 12;
    public static final String DEFAULT_SCHEMA  = "public";

    // Códigos de error PostgreSQL
    public static final String PG_UNIQUE_VIOLATION = "23505";
    public static final String PG_FK_VIOLATION     = "23503";

    // Nombres de caché Redis
    public static final String CACHE_SESSIONS   = "sessions";
    public static final String CACHE_ARTICLES   = "articles";
    public static final String CACHE_EMPLOYEES  = "employees";
    public static final String CACHE_CLIENTES   = "clientes";
    public static final String CACHE_DASHBOARD  = "dashboard";
}
