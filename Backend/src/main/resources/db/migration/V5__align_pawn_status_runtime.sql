-- Alinea DEFAULT, índices y fn_expire_overdue_pawns() con PawnStatus JPA (ACTIVO/VENCIDO).

ALTER TABLE public.pawns
    ALTER COLUMN status SET DEFAULT 'ACTIVO';

CREATE OR REPLACE FUNCTION public.fn_expire_overdue_pawns()
RETURNS INT LANGUAGE plpgsql AS $$
DECLARE
    v_count INT;
BEGIN
    UPDATE public.pawns
    SET    status     = 'VENCIDO',
           updated_at = NOW()
    WHERE  status     = 'ACTIVO'
      AND  return_date < CURRENT_DATE;

    GET DIAGNOSTICS v_count = ROW_COUNT;
    RETURN v_count;
END;
$$;

DROP INDEX IF EXISTS public.idx_pawns_return_date;
DROP INDEX IF EXISTS public.idx_pawns_active_return;

CREATE INDEX IF NOT EXISTS idx_pawns_return_date
    ON public.pawns (return_date) WHERE status = 'ACTIVO';
CREATE INDEX IF NOT EXISTS idx_pawns_active_return
    ON public.pawns (return_date) WHERE status = 'ACTIVO';
