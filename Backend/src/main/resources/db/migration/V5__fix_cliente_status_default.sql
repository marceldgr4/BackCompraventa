-- Fix default value for status column in clientes table
ALTER TABLE public.clientes ALTER COLUMN status SET DEFAULT 'ACTIVO'::public.cliente_status;

-- Update any existing records that might have 'Activo' incorrectly set (if possible, though enum validation would have rejected it)
UPDATE public.clientes SET status = 'ACTIVO' WHERE status::text = 'Activo';
