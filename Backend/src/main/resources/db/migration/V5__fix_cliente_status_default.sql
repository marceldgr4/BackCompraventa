-- =============================================================================
-- V5__fix_cliente_status_default.sql
--
-- Corrige la inconsistencia en V1__initial_schema.sql donde el DEFAULT de la
-- columna status es 'Activo' pero el enum cliente_status define ('ACTIVO','INACTIVO').
--
-- PostgreSQL es case-sensitive en enums: 'Activo' ≠ 'ACTIVO' → error de constraint
-- en cada INSERT sin status explícito.
--
-- ⚠️ Si tu BD ya fue creada con V1 y tiene datos, este script los migra.
-- =============================================================================

-- Corregir el valor por defecto de la columna status en clientes
ALTER TABLE public.clientes
    ALTER COLUMN status SET DEFAULT 'ACTIVO';

-- Corregir registros existentes que puedan tener el valor legacy 'Activo'
-- (sólo aplica si se corrió V1 con el DEFAULT incorrecto y se insertaron filas)
UPDATE public.clientes
SET status = 'ACTIVO'
WHERE status::text = 'Activo';

UPDATE public.clientes
SET status = 'INACTIVO'
WHERE status::text = 'Inactivo' OR status::text = 'Eliminado';