-- Equivalent of the EF Core migrations
-- (20260305154855_RecreateDeletedTable + 20260305182141_AddAvatarUrl)
-- collapsed into a single authoritative starting schema, since this is a
-- fresh Postgres database rather than a migrated-forward SQL Server one.

CREATE TABLE users (
                       id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       username        VARCHAR(100) NOT NULL,
                       email           VARCHAR(255) NOT NULL,
                       password_hash   TEXT NOT NULL,
                       avatar_url      TEXT,
                       created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_users_email ON users (email);