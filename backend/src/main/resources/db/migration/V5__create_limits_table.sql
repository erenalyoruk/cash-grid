CREATE TABLE limits (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    role                VARCHAR(20)     NOT NULL CHECK (role IN ('MAKER', 'CHECKER', 'ADMIN')),
    max_single_amount   DECIMAL(18, 2)  NOT NULL CHECK (max_single_amount > 0),
    max_daily_amount    DECIMAL(18, 2)  NOT NULL CHECK (max_daily_amount > 0),
    currency            VARCHAR(3)      NOT NULL DEFAULT 'TRY',
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT now(),
    UNIQUE (role, currency)
);

-- Default limits
INSERT INTO limits (role, max_single_amount, max_daily_amount, currency)
VALUES
    ('MAKER', 100000.00, 500000.00, 'TRY'),
    ('MAKER', 10000.00, 50000.00, 'USD'),
    ('MAKER', 10000.00, 50000.00, 'EUR');
