-- 1. Añadir is_deleted a BaseEntity relacionadas
ALTER TABLE clientes ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. Refactor employees: UUID to BIGSERIAL
-- Rename id to global_id
ALTER TABLE employees RENAME COLUMN id TO global_id;

-- Add new id BIGSERIAL
ALTER TABLE employees ADD COLUMN id BIGSERIAL;

-- 1. Drop foreign key constraints referencing employees(id)
ALTER TABLE pawns DROP CONSTRAINT pawns_employee_id_fkey;
ALTER TABLE pawn_payments DROP CONSTRAINT pawn_payments_created_by_employee_id_fkey;
ALTER TABLE sales DROP CONSTRAINT sales_employee_id_fkey;
ALTER TABLE purchases DROP CONSTRAINT purchases_employee_id_fkey;

-- 2. Drop PK on employees
ALTER TABLE employees DROP CONSTRAINT employees_pkey;

-- 3. Make id the new PK
ALTER TABLE employees ADD PRIMARY KEY (id);

-- 4. Make global_id UNIQUE
ALTER TABLE employees ADD CONSTRAINT employees_global_id_key UNIQUE (global_id);

-- 5. Add is_deleted to employees
ALTER TABLE employees ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Now we need to update foreign keys in other tables.
-- pawns
ALTER TABLE pawns ADD COLUMN new_employee_id BIGINT;
UPDATE pawns p SET new_employee_id = e.id FROM employees e WHERE p.employee_id = e.global_id;
ALTER TABLE pawns DROP COLUMN employee_id;
ALTER TABLE pawns RENAME COLUMN new_employee_id TO employee_id;
ALTER TABLE pawns ALTER COLUMN employee_id SET NOT NULL;
ALTER TABLE pawns ADD CONSTRAINT pawns_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES employees(id);

-- pawn_payments
ALTER TABLE pawn_payments ADD COLUMN new_employee_id BIGINT;
UPDATE pawn_payments p SET new_employee_id = e.id FROM employees e WHERE p.created_by_employee_id = e.global_id;
ALTER TABLE pawn_payments DROP COLUMN created_by_employee_id;
ALTER TABLE pawn_payments RENAME COLUMN new_employee_id TO created_by_employee_id;
ALTER TABLE pawn_payments ADD CONSTRAINT pawn_payments_created_by_employee_id_fkey FOREIGN KEY (created_by_employee_id) REFERENCES employees(id);

-- sales
ALTER TABLE sales ADD COLUMN new_employee_id BIGINT;
UPDATE sales s SET new_employee_id = e.id FROM employees e WHERE s.employee_id = e.global_id;
ALTER TABLE sales DROP COLUMN employee_id;
ALTER TABLE sales RENAME COLUMN new_employee_id TO employee_id;
ALTER TABLE sales ALTER COLUMN employee_id SET NOT NULL;
ALTER TABLE sales ADD CONSTRAINT sales_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES employees(id);

-- purchases
ALTER TABLE purchases ADD COLUMN new_employee_id BIGINT;
UPDATE purchases p SET new_employee_id = e.id FROM employees e WHERE p.employee_id = e.global_id;
ALTER TABLE purchases DROP COLUMN employee_id;
ALTER TABLE purchases RENAME COLUMN new_employee_id TO employee_id;
ALTER TABLE purchases ALTER COLUMN employee_id SET NOT NULL;
ALTER TABLE purchases ADD CONSTRAINT purchases_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES employees(id);
