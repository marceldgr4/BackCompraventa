-- =============================================================================
-- V4__add_missing_indexes_and_constraints.sql
-- Índices adicionales de rendimiento y constraints que no estaban en V1.
-- =============================================================================

-- Índice en phone para el check de duplicados en clientes (HU-CLI-01)
CREATE INDEX IF NOT EXISTS idx_clientes_phone
    ON public.clientes (phone) WHERE phone IS NOT NULL;

-- Índice en email de clientes para búsquedas
CREATE INDEX IF NOT EXISTS idx_clientes_email
    ON public.clientes (email) WHERE email IS NOT NULL;

-- Índice compuesto para búsquedas por nombre en clientes
CREATE INDEX IF NOT EXISTS idx_clientes_nombre
    ON public.clientes (LOWER(first_name), LOWER(last_name));

-- Índice en articles para filtro de stock disponible (HU-ART-01)
CREATE INDEX IF NOT EXISTS idx_articles_source_type
    ON public.articles (source_type);

-- Índice en pawns para el scheduler de vencimiento (fn_expire_overdue_pawns)
CREATE INDEX IF NOT EXISTS idx_pawns_active_return
    ON public.pawns (return_date)
    WHERE status = 'Activo';

-- Índice en purchases para filtros por fecha
CREATE INDEX IF NOT EXISTS idx_purchases_date
    ON public.purchases (purchase_date DESC);

-- Índice en sales_details para cálculo de totales por venta
CREATE INDEX IF NOT EXISTS idx_sales_details_article
    ON public.sales_details (article_id);