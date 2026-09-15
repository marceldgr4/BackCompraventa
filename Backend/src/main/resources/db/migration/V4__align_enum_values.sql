-- Añade labels que JPA escribe con EnumType.STRING.
-- ADD VALUE no puede usarse en la misma transacción: esta migración solo añade valores.

ALTER TYPE public.pawn_status ADD VALUE IF NOT EXISTS 'VENDIDO';
ALTER TYPE public.source_type ADD VALUE IF NOT EXISTS 'EMPEÑO';
ALTER TYPE public.source_type ADD VALUE IF NOT EXISTS 'OTROS';
