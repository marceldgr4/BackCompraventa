-- =============================================================================
-- V2__add_sync_triggers.sql
-- Triggers that capture every INSERT / UPDATE / DELETE on business tables
-- and queue the change in sync_outbox for upload to Supabase.
-- =============================================================================

-- ── Trigger function ──────────────────────────────────────────────────────────

CREATE OR REPLACE FUNCTION public.fn_capture_sync_outbox()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
v_entity_id  UUID;
    v_operation  VARCHAR(10);
    v_payload    JSONB;
BEGIN
    -- Determine operation
    v_operation := TG_OP;  -- 'INSERT' | 'UPDATE' | 'DELETE'

    -- Extract global_id (used as the cross-system identity key)
    IF TG_OP = 'DELETE' THEN
        v_entity_id := OLD.global_id;
        v_payload   := to_jsonb(OLD);
ELSE
        v_entity_id := NEW.global_id;
        v_payload   := to_jsonb(NEW);
END IF;

INSERT INTO public.sync_outbox
(entity_type, entity_id, operation, payload, local_version, status)
VALUES
    (TG_TABLE_NAME, v_entity_id, v_operation, v_payload, 1, 'PENDING');

RETURN NULL;  -- AFTER trigger: return value is ignored
END;
$$;

-- ── Apply to business tables ──────────────────────────────────────────────────

DO $$
DECLARE tbl TEXT;
BEGIN
    FOREACH tbl IN ARRAY ARRAY['articles', 'clientes', 'pawns', 'sales', 'purchases'] LOOP
        EXECUTE format(
            'DROP TRIGGER IF EXISTS trg_%s_sync ON public.%s;
             CREATE TRIGGER trg_%s_sync
             AFTER INSERT OR UPDATE OR DELETE ON public.%s
             FOR EACH ROW EXECUTE FUNCTION public.fn_capture_sync_outbox();',
            tbl, tbl, tbl, tbl
        );
END LOOP;
END $$;

-- ── Auto-expire overdue pawns function ───────────────────────────────────────

CREATE OR REPLACE FUNCTION public.fn_expire_overdue_pawns()
RETURNS INT LANGUAGE plpgsql AS $$
DECLARE
v_count INT;
BEGIN
UPDATE public.pawns
SET    status     = 'Vencido',
       updated_at = NOW()
WHERE  status     = 'Activo'
  AND  return_date < CURRENT_DATE;

GET DIAGNOSTICS v_count = ROW_COUNT;
RETURN v_count;
END;
$$;

-- ── Stored procedure: register_sale (atomic sale + stock deduction) ────────────

CREATE OR REPLACE PROCEDURE public.register_sale(
    p_employee_id          UUID,
    p_cliente_id           BIGINT,
    p_cliente_nombre_anon  TEXT,
    p_notes                TEXT,
    p_items                JSONB,   -- [{"article_id":1, "amount":2, "unit_price":50.00}, ...]
    OUT p_sale_id          BIGINT
)
LANGUAGE plpgsql AS $$
DECLARE
v_item       JSONB;
    v_article_id BIGINT;
    v_amount     INT;
    v_unit_price NUMERIC(12,2);
    v_stock      INT;
BEGIN
    -- Create the sale header
INSERT INTO public.sales (employee_id, cliente_id, cliente_nombre_anon, notes)
VALUES (p_employee_id, p_cliente_id, p_cliente_nombre_anon, p_notes)
    RETURNING id INTO p_sale_id;

-- Process each item in the JSON array
FOR v_item IN SELECT * FROM jsonb_array_elements(p_items) LOOP
    v_article_id := (v_item->>'article_id')::BIGINT;
v_amount     := (v_item->>'amount')::INT;
        v_unit_price := (v_item->>'unit_price')::NUMERIC(12,2);

        -- Pessimistic lock to prevent overselling
SELECT amount INTO v_stock
FROM public.articles
WHERE id = v_article_id
    FOR UPDATE;

IF v_stock IS NULL THEN
            RAISE EXCEPTION 'Artículo % no encontrado', v_article_id;
END IF;

        IF v_stock < v_amount THEN
            RAISE EXCEPTION 'Stock insuficiente para artículo %. Disponible: %, requerido: %',
                v_article_id, v_stock, v_amount;
END IF;

        -- Deduct stock
UPDATE public.articles
SET    amount     = amount - v_amount,
       updated_at = NOW()
WHERE  id = v_article_id;

-- Record detail
INSERT INTO public.sales_details (sale_id, article_id, amount, unit_price)
VALUES (p_sale_id, v_article_id, v_amount, v_unit_price);
END LOOP;
END;
$$;