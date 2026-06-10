-- =============================================================================
-- V2__seed_admin.sql
--
-- Datos iniciales: cuenta Admin por defecto para primera configuración.
-- La contraseña 'Admin1234!' está hasheada con BCrypt (strength 12).
--
-- ⚠️  CAMBIE ESTA CONTRASEÑA INMEDIATAMENTE después del primer login.
-- =============================================================================

INSERT INTO public.employees (email, full_name, password_hash, rol, active)
VALUES (
    'admin@compraventa.local',
    'Administrador Principal',
    -- BCrypt hash of 'Admin1234!' strength=12
    '$2a$12$pOHhF3RDRlD9Wa/cWYS2cuRRrOxZQW5Kf7D/LYyT9NSSuXRHyDZ2i',
    'ADMIN',
    TRUE
)
ON CONFLICT (email) DO NOTHING;
