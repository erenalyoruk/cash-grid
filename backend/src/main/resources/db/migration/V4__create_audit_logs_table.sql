CREATE TABLE audit_logs (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type     VARCHAR(50)     NOT NULL,
    entity_id       UUID            NOT NULL,
    action          VARCHAR(30)     NOT NULL,
    performed_by    UUID            NOT NULL REFERENCES users(id),
    correlation_id  VARCHAR(64),
    details         JSONB,
    created_at      TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_performed_by ON audit_logs (performed_by);
CREATE INDEX idx_audit_action ON audit_logs (action);
CREATE INDEX idx_audit_created_at ON audit_logs (created_at DESC);
CREATE INDEX idx_audit_correlation ON audit_logs (correlation_id);
