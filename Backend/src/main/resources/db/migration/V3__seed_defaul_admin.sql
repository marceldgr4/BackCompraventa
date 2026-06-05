-- =============================================================================
-- V3__seed_default_admin.sql
-- Seeds one default Admin account for first-time setup.
-- The password 'Admin1234!' is BCrypt-hashed (strength 12).
--
-- ⚠️  CHANGE THIS PASSWORD IMMEDIATELY after first login.
-- =============================================================================

INSERT INTO public.employees (id, email, full_name, password_hash, rol, active)
VALUES (
           gen_random_uuid(),
           'admin@compraventa.local',
           'Administrador Principal',
           -- BCrypt hash of 'Admin1234!' strength=12
           '$2a$12$pOHhF3RDRlD9Wa/cWYS2cuRRrOxZQW5Kf7D/LYyT9NSSuXRHyDZ2i',
           'ADMIN',
           TRUE
       )
    ON CONFLICT (email) DO NOTHING;