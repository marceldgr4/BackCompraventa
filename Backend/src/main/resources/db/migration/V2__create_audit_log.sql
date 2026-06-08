CREATE TABLE IF NOT EXISTS public.audit_log (
    id BIGSERIAL PRIMARY KEY,
    employee_id VARCHAR(255),
    operation VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id VARCHAR(255),
    before_value TEXT,
    after_value TEXT,
    error_message TEXT,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL
);
