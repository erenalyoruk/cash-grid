CREATE TABLE payments (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key     VARCHAR(64)     NOT NULL UNIQUE,
    source_account_id   UUID            NOT NULL REFERENCES accounts(id),
    target_account_id   UUID            NOT NULL REFERENCES accounts(id),
    amount              DECIMAL(18, 2)  NOT NULL CHECK (amount > 0),
    currency            VARCHAR(3)      NOT NULL DEFAULT 'TRY',
    description         VARCHAR(255),
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'PROCESSING', 'COMPLETED', 'FAILED')),
    created_by          UUID            NOT NULL REFERENCES users(id),
    approved_by         UUID            REFERENCES users(id),
    rejection_reason    VARCHAR(500),
    created_at          TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_status ON payments (status);
CREATE INDEX idx_payments_idempotency ON payments (idempotency_key);
CREATE INDEX idx_payments_source ON payments (source_account_id);
CREATE INDEX idx_payments_target ON payments (target_account_id);
CREATE INDEX idx_payments_created_by ON payments (created_by);
CREATE INDEX idx_payments_created_at ON payments (created_at DESC);

-- Maker cannot approve own payment
ALTER TABLE payments ADD CONSTRAINT chk_maker_checker
    CHECK (created_by IS DISTINCT FROM approved_by);
