-- =============================================================================
-- V1__schema_completo.sql
--
-- Schema completo y definitivo del sistema CompraVenta.
-- Consolida: enums, tablas, índices, triggers, funciones y procedimientos.
--
-- Generado el 2026-06-09 fusionando V1..V7 anteriores.
-- =============================================================================


-- ═══════════════════════════════════════════════════════════════════════════════
-- 1. TIPOS ENUM
-- ═══════════════════════════════════════════════════════════════════════════════

DO $$ BEGIN
    CREATE TYPE role_user         AS ENUM ('ADMIN', 'EMPLEADO');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE article_category AS ENUM ('Electrodomesticos', 'Joyeria', 'Herramientas', 'Tecnologia', 'Otro');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE source_type      AS ENUM ('EMPENO', 'COMPRA', 'AJUSTE', 'OTRO');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE item_state       AS ENUM ('Excelente', 'Bueno', 'Regular', 'Malo');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE pawn_status      AS ENUM ('Activo', 'Vencido', 'Finalizado', 'Retirado', 'Perdido', 'Vendido');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE cliente_status   AS ENUM ('ACTIVO', 'INACTIVO', 'ELIMINADO');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE registration_type AS ENUM ('COMPLETO', 'RAPIDO');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;


-- ═══════════════════════════════════════════════════════════════════════════════
-- 2. TABLAS
-- ═══════════════════════════════════════════════════════════════════════════════

-- ── employees ────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS public.employees (
    id            BIGSERIAL   PRIMARY KEY,
    global_id     UUID        NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    email         TEXT        NOT NULL UNIQUE,
    full_name     TEXT        NOT NULL,
    password_hash TEXT        NOT NULL,
    rol           role_user   NOT NULL DEFAULT 'EMPLEADO',
    active        BOOLEAN     NOT NULL DEFAULT TRUE,
    is_deleted    BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── clientes ─────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS public.clientes (
    id                BIGSERIAL         PRIMARY KEY,
    global_id         UUID              NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    cedula            TEXT              UNIQUE,
    first_name        TEXT              NOT NULL,
    last_name         TEXT,
    email             TEXT,
    phone             TEXT,
    address           TEXT,
    city              TEXT,
    status            cliente_status    NOT NULL DEFAULT 'ACTIVO',
    registration_type registration_type NOT NULL DEFAULT 'COMPLETO',
    is_deleted        BOOLEAN           NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ       NOT NULL DEFAULT NOW()
);

-- ── articles ─────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS public.articles (
    id             BIGSERIAL         PRIMARY KEY,
    global_id      UUID              NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    cliente_id     BIGINT            REFERENCES public.clientes(id) ON DELETE SET NULL,
    name_article   TEXT              NOT NULL,
    description    TEXT,
    category       article_category  NOT NULL,
    source_type    source_type,
    item_state     item_state,
    amount         INT               NOT NULL DEFAULT 0 CHECK (amount >= 0),
    price          NUMERIC(12, 2)    NOT NULL CHECK (price > 0),
    purchase_price NUMERIC(12, 2),
    created_at     TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ       NOT NULL DEFAULT NOW()
);

-- ── pawns ────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS public.pawns (
    id                  BIGSERIAL      PRIMARY KEY,
    global_id           UUID           NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    employee_id         BIGINT         NOT NULL REFERENCES public.employees(id),
    article_id          BIGINT         NOT NULL REFERENCES public.articles(id),
    cliente_id          BIGINT         NOT NULL REFERENCES public.clientes(id),
    amount              INT            NOT NULL CHECK (amount > 0),
    price               NUMERIC(12,2)  NOT NULL CHECK (price > 0),
    weight_grams        NUMERIC(10,2),
    installment_count   INT            NOT NULL DEFAULT 1 CHECK (installment_count >= 1),
    installments_paid   INT            NOT NULL DEFAULT 0,
    installments_missed INT            NOT NULL DEFAULT 0,
    pawn_date           DATE           NOT NULL,
    return_date         DATE           NOT NULL,
    status              pawn_status    NOT NULL DEFAULT 'Activo',
    notes               TEXT,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_return_after_pawn CHECK (return_date > pawn_date)
);

-- ── pawn_payments ────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS public.pawn_payments (
    id                     BIGSERIAL      PRIMARY KEY,
    pawn_id                BIGINT         NOT NULL REFERENCES public.pawns(id) ON DELETE CASCADE,
    amount                 NUMERIC(12,2)  NOT NULL DEFAULT 0 CHECK (amount >= 0),
    payment_date           DATE           NOT NULL,
    notes                  TEXT,
    created_by_employee_id BIGINT         REFERENCES public.employees(id),
    is_missed              BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at             TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

-- ── sales ────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS public.sales (
    id                  BIGSERIAL   PRIMARY KEY,
    global_id           UUID        NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    employee_id         BIGINT      NOT NULL REFERENCES public.employees(id),
    cliente_id          BIGINT      REFERENCES public.clientes(id) ON DELETE SET NULL,
    cliente_nombre_anon TEXT,
    sale_date           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    notes               TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── sales_details ────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS public.sales_details (
    id         BIGSERIAL     PRIMARY KEY,
    sale_id    BIGINT        NOT NULL REFERENCES public.sales(id) ON DELETE CASCADE,
    article_id BIGINT        NOT NULL REFERENCES public.articles(id),
    amount     INT           NOT NULL CHECK (amount > 0),
    unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price > 0)
);

-- ── purchases ────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS public.purchases (
    id             BIGSERIAL     PRIMARY KEY,
    global_id      UUID          NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    employee_id    BIGINT        NOT NULL REFERENCES public.employees(id),
    cliente_id     BIGINT        REFERENCES public.clientes(id) ON DELETE SET NULL,
    article_id     BIGINT        NOT NULL REFERENCES public.articles(id),
    purchase_price NUMERIC(12,2) NOT NULL CHECK (purchase_price > 0),
    purchase_date  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    notes          TEXT,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

-- ── audit_log ────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS public.audit_log (
    id            BIGSERIAL    PRIMARY KEY,
    employee_id   TEXT,
    operation     TEXT         NOT NULL,
    entity_type   TEXT,
    entity_id     TEXT,
    before_value  TEXT,
    after_value   TEXT,
    error_message TEXT,
    ip_address    VARCHAR(45),
    timestamp     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ── sync_outbox ──────────────────────────────────────────────────────────────
-- status es VARCHAR(20), NO un tipo enum de PostgreSQL.
-- @Enumerated(EnumType.STRING) de JPA escribe strings directamente.

CREATE TABLE IF NOT EXISTS public.sync_outbox (
    id            BIGSERIAL   PRIMARY KEY,
    entity_type   VARCHAR(50) NOT NULL,
    entity_id     UUID        NOT NULL,
    operation     VARCHAR(10) NOT NULL CHECK (operation IN ('INSERT','UPDATE','DELETE')),
    payload       JSONB       NOT NULL,
    local_version BIGINT      NOT NULL DEFAULT 1,
    cloud_version BIGINT,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                  CHECK (status IN ('PENDING','SYNCING','SYNCED','CONFLICT','FAILED')),
    retry_count   INT         NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    synced_at     TIMESTAMPTZ
);

-- ── sync_log ─────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS public.sync_log (
    id          BIGSERIAL   PRIMARY KEY,
    sync_run_id UUID        NOT NULL DEFAULT gen_random_uuid(),
    direction   VARCHAR(10) NOT NULL CHECK (direction IN ('UPLOAD','DOWNLOAD')),
    entity_type VARCHAR(50),
    records_ok  INT         NOT NULL DEFAULT 0,
    records_err INT         NOT NULL DEFAULT 0,
    conflicts   INT         NOT NULL DEFAULT 0,
    started_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    finished_at TIMESTAMPTZ,
    details     JSONB
);


-- ═══════════════════════════════════════════════════════════════════════════════
-- 3. ÍNDICES
-- ═══════════════════════════════════════════════════════════════════════════════

-- employees
CREATE INDEX IF NOT EXISTS idx_employees_email      ON public.employees (LOWER(email));
CREATE INDEX IF NOT EXISTS idx_employees_active     ON public.employees (active);

-- clientes
CREATE INDEX IF NOT EXISTS idx_clientes_global_id   ON public.clientes (global_id);
CREATE INDEX IF NOT EXISTS idx_clientes_cedula      ON public.clientes (cedula)    WHERE cedula IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_clientes_status      ON public.clientes (status);
CREATE INDEX IF NOT EXISTS idx_clientes_phone       ON public.clientes (phone)     WHERE phone IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_clientes_email       ON public.clientes (email)     WHERE email IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_clientes_nombre      ON public.clientes (LOWER(first_name), LOWER(last_name));

-- articles
CREATE INDEX IF NOT EXISTS idx_articles_global_id   ON public.articles (global_id);
CREATE INDEX IF NOT EXISTS idx_articles_name        ON public.articles (LOWER(name_article));
CREATE INDEX IF NOT EXISTS idx_articles_category    ON public.articles (category);
CREATE INDEX IF NOT EXISTS idx_articles_amount      ON public.articles (amount);
CREATE INDEX IF NOT EXISTS idx_articles_source_type ON public.articles (source_type);

-- pawns
CREATE INDEX IF NOT EXISTS idx_pawns_global_id      ON public.pawns (global_id);
CREATE INDEX IF NOT EXISTS idx_pawns_status         ON public.pawns (status);
CREATE INDEX IF NOT EXISTS idx_pawns_employee       ON public.pawns (employee_id);
CREATE INDEX IF NOT EXISTS idx_pawns_return_date    ON public.pawns (return_date) WHERE status = 'Activo';
CREATE INDEX IF NOT EXISTS idx_pawns_active_return  ON public.pawns (return_date) WHERE status = 'Activo';

-- pawn_payments
CREATE INDEX IF NOT EXISTS idx_pawn_payments_pawn_id ON public.pawn_payments (pawn_id);

-- sales
CREATE INDEX IF NOT EXISTS idx_sales_global_id      ON public.sales (global_id);
CREATE INDEX IF NOT EXISTS idx_sales_employee       ON public.sales (employee_id);
CREATE INDEX IF NOT EXISTS idx_sales_sale_date      ON public.sales (sale_date);

-- sales_details
CREATE INDEX IF NOT EXISTS idx_sales_details_sale_id  ON public.sales_details (sale_id);
CREATE INDEX IF NOT EXISTS idx_sales_details_article  ON public.sales_details (article_id);

-- purchases
CREATE INDEX IF NOT EXISTS idx_purchases_global_id  ON public.purchases (global_id);
CREATE INDEX IF NOT EXISTS idx_purchases_employee   ON public.purchases (employee_id);
CREATE INDEX IF NOT EXISTS idx_purchases_date       ON public.purchases (purchase_date DESC);

-- audit_log
CREATE INDEX IF NOT EXISTS idx_audit_log_employee   ON public.audit_log (employee_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_timestamp  ON public.audit_log (timestamp);

-- sync_outbox
CREATE INDEX IF NOT EXISTS idx_sync_outbox_status     ON public.sync_outbox (status);
CREATE INDEX IF NOT EXISTS idx_sync_outbox_entity     ON public.sync_outbox (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_sync_outbox_created_at ON public.sync_outbox (created_at);


-- ═══════════════════════════════════════════════════════════════════════════════
-- 4. FUNCIONES Y TRIGGERS
-- ═══════════════════════════════════════════════════════════════════════════════

-- ── Función: actualizar updated_at automáticamente ───────────────────────────

CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

DO $$
DECLARE tbl TEXT;
BEGIN
    FOREACH tbl IN ARRAY ARRAY['employees','clientes','articles','pawns'] LOOP
        EXECUTE format(
            'DROP TRIGGER IF EXISTS trg_%s_updated_at ON public.%s;
             CREATE TRIGGER trg_%s_updated_at
             BEFORE UPDATE ON public.%s
             FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();',
            tbl, tbl, tbl, tbl
        );
    END LOOP;
END $$;

-- ── Función: captura de cambios para sync_outbox ─────────────────────────────

CREATE OR REPLACE FUNCTION public.fn_capture_sync_outbox()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    v_entity_id  UUID;
    v_operation  VARCHAR(10);
    v_payload    JSONB;
BEGIN
    v_operation := TG_OP;  -- 'INSERT' | 'UPDATE' | 'DELETE'

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

-- ── Función: expirar empeños vencidos ────────────────────────────────────────

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


-- ═══════════════════════════════════════════════════════════════════════════════
-- 5. PROCEDIMIENTOS ALMACENADOS
-- ═══════════════════════════════════════════════════════════════════════════════

-- ── Procedimiento: registrar venta (atómica: cabecera + detalles + stock) ────

CREATE OR REPLACE PROCEDURE public.register_sale(
    p_employee_id          BIGINT,
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
    -- Crear cabecera de venta
    INSERT INTO public.sales (employee_id, cliente_id, cliente_nombre_anon, notes)
    VALUES (p_employee_id, p_cliente_id, p_cliente_nombre_anon, p_notes)
    RETURNING id INTO p_sale_id;

    -- Procesar cada ítem del array JSON
    FOR v_item IN SELECT * FROM jsonb_array_elements(p_items) LOOP
        v_article_id := (v_item->>'article_id')::BIGINT;
        v_amount     := (v_item->>'amount')::INT;
        v_unit_price := (v_item->>'unit_price')::NUMERIC(12,2);

        -- Lock pesimista para evitar sobreventa
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

        -- Descontar stock
        UPDATE public.articles
        SET    amount     = amount - v_amount,
               updated_at = NOW()
        WHERE  id = v_article_id;

        -- Registrar detalle
        INSERT INTO public.sales_details (sale_id, article_id, amount, unit_price)
        VALUES (p_sale_id, v_article_id, v_amount, v_unit_price);
    END LOOP;
END;
$$;
