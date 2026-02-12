CREATE TABLE accounts (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_name   VARCHAR(100)    NOT NULL,
    iban            VARCHAR(26)     NOT NULL UNIQUE,
    currency        VARCHAR(3)      NOT NULL DEFAULT 'TRY',
    balance         DECIMAL(18, 2)  NOT NULL DEFAULT 0.00 CHECK (balance >= 0),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE INDEX idx_accounts_iban ON accounts (iban);
CREATE INDEX idx_accounts_currency ON accounts (currency);
