-- =============================================================================
-- V3__align_base_entity_columns.sql
--
-- BaseEntity mapea is_deleted (y updated_at) en JPA. articles, pawns y sales
-- no tenían esas columnas, lo que rompe hibernate.ddl-auto=validate y el
-- INSERT de artículos al registrar una compra.
-- =============================================================================

ALTER TABLE public.articles
    ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE public.pawns
    ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE public.sales
    ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE public.sales
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
