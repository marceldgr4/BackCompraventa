
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
CREATE TYPE cliente_status    AS ENUM ('ACTIVO', 'INACTIVO');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
CREATE TYPE registration_type AS ENUM ('COMPLETO', 'RAPIDO');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- ── employees ─────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS public.employees (
                                                id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    email         TEXT        NOT NULL UNIQUE,
    full_name     TEXT        NOT NULL,
    password_hash TEXT        NOT NULL,
    rol           role_user   NOT NULL DEFAULT 'EMPLEADO',
    active        BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_employees_email  ON public.employees (LOWER(email));
CREATE INDEX IF NOT EXISTS idx_employees_active ON public.employees (active);

-- ── clientes ──────────────────────────────────────────────────────────────────

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
status            cliente_status    NOT NULL DEFAULT 'Activo',
registration_type registration_type NOT NULL DEFAULT 'COMPLETO',
created_at        TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
updated_at        TIMESTAMPTZ       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_clientes_global_id ON public.clientes (global_id);
CREATE INDEX IF NOT EXISTS idx_clientes_cedula    ON public.clientes (cedula) WHERE cedula IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_clientes_status    ON public.clientes (status);

-- ── articles ──────────────────────────────────────────────────────────────────

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

CREATE INDEX IF NOT EXISTS idx_articles_global_id ON public.articles (global_id);
CREATE INDEX IF NOT EXISTS idx_articles_name      ON public.articles (LOWER(name_article));
CREATE INDEX IF NOT EXISTS idx_articles_category  ON public.articles (category);
CREATE INDEX IF NOT EXISTS idx_articles_amount    ON public.articles (amount);

-- ── pawns ─────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS public.pawns (
                                            id                  BIGSERIAL    PRIMARY KEY,
                                            global_id           UUID         NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    employee_id         UUID         NOT NULL REFERENCES public.employees(id),
    article_id          BIGINT       NOT NULL REFERENCES public.articles(id),
    cliente_id          BIGINT       NOT NULL REFERENCES public.clientes(id),
    amount              INT          NOT NULL CHECK (amount > 0),
    price               NUMERIC(12,2) NOT NULL CHECK (price > 0),
    weight_grams        NUMERIC(10,2),
    installment_count   INT          NOT NULL DEFAULT 1 CHECK (installment_count >= 1),
    installments_paid   INT          NOT NULL DEFAULT 0,
    installments_missed INT          NOT NULL DEFAULT 0,
    pawn_date           DATE         NOT NULL,
    return_date         DATE         NOT NULL,
    status              pawn_status  NOT NULL DEFAULT 'Activo',
    notes               TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_return_after_pawn CHECK (return_date > pawn_date)
    );

CREATE INDEX IF NOT EXISTS idx_pawns_global_id   ON public.pawns (global_id);
CREATE INDEX IF NOT EXISTS idx_pawns_status      ON public.pawns (status);
CREATE INDEX IF NOT EXISTS idx_pawns_employee    ON public.pawns (employee_id);
CREATE INDEX IF NOT EXISTS idx_pawns_return_date ON public.pawns (return_date) WHERE status = 'Activo';

-- ── pawn_payments ─────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS public.pawn_payments (
                                                    id                      BIGSERIAL    PRIMARY KEY,
                                                    pawn_id                 BIGINT       NOT NULL REFERENCES public.pawns(id) ON DELETE CASCADE,
    amount                  NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (amount >= 0),
    payment_date            DATE         NOT NULL,
    notes                   TEXT,
    created_by_employee_id  UUID         REFERENCES public.employees(id),
    is_missed               BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_pawn_payments_pawn_id ON public.pawn_payments (pawn_id);

-- ── sales ─────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS public.sales (
                                            id                   BIGSERIAL   PRIMARY KEY,
                                            global_id            UUID        NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    employee_id          UUID        NOT NULL REFERENCES public.employees(id),
    cliente_id           BIGINT      REFERENCES public.clientes(id) ON DELETE SET NULL,
    cliente_nombre_anon  TEXT,
    sale_date            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    notes                TEXT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_sales_global_id ON public.sales (global_id);
CREATE INDEX IF NOT EXISTS idx_sales_employee  ON public.sales (employee_id);
CREATE INDEX IF NOT EXISTS idx_sales_sale_date ON public.sales (sale_date);

-- ── sales_details ─────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS public.sales_details (
                                                    id         BIGSERIAL     PRIMARY KEY,
                                                    sale_id    BIGINT        NOT NULL REFERENCES public.sales(id) ON DELETE CASCADE,
    article_id BIGINT        NOT NULL REFERENCES public.articles(id),
    amount     INT           NOT NULL CHECK (amount > 0),
    unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price > 0)
    );

CREATE INDEX IF NOT EXISTS idx_sales_details_sale_id ON public.sales_details (sale_id);

-- ── purchases ─────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS public.purchases (
                                                id             BIGSERIAL     PRIMARY KEY,
                                                global_id      UUID          NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    employee_id    UUID          NOT NULL REFERENCES public.employees(id),
    cliente_id     BIGINT        REFERENCES public.clientes(id) ON DELETE SET NULL,
    article_id     BIGINT        NOT NULL REFERENCES public.articles(id),
    purchase_price NUMERIC(12,2) NOT NULL CHECK (purchase_price > 0),
    purchase_date  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    notes          TEXT,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_purchases_global_id ON public.purchases (global_id);
CREATE INDEX IF NOT EXISTS idx_purchases_employee  ON public.purchases (employee_id);

-- ── audit_log ─────────────────────────────────────────────────────────────────

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

CREATE INDEX IF NOT EXISTS idx_audit_log_employee  ON public.audit_log (employee_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_timestamp ON public.audit_log (timestamp);

-- ── sync_outbox ───────────────────────────────────────────────────────────────
-- FIX CLAVE: status es VARCHAR(20), NO un tipo enum de PostgreSQL.
-- @Enumerated(EnumType.STRING) de JPA escribe strings directamente,
-- y PostgreSQL acepta VARCHAR sin necesitar cast explícito.

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

CREATE INDEX IF NOT EXISTS idx_sync_outbox_status     ON public.sync_outbox (status);
CREATE INDEX IF NOT EXISTS idx_sync_outbox_entity     ON public.sync_outbox (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_sync_outbox_created_at ON public.sync_outbox (created_at);

-- ── sync_log ──────────────────────────────────────────────────────────────────

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

-- ── Función: actualizar updated_at automáticamente ────────────────────────────

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