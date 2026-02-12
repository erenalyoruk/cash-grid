CREATE TABLE users (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    username        VARCHAR(50)     NOT NULL UNIQUE,
    email           VARCHAR(100)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    role            VARCHAR(20)     NOT NULL CHECK (role IN ('MAKER', 'CHECKER', 'ADMIN')),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role);
