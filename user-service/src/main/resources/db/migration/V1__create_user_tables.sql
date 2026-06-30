CREATE TABLE user_profiles (
                               user_id         UUID PRIMARY KEY,
                               username        VARCHAR(100) NOT NULL,
                               bio             VARCHAR(500),
                               avatar_url      TEXT,
                               created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE follows (
                         id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         follower_id     UUID NOT NULL,
                         following_id    UUID NOT NULL,
                         created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_follows_follower_following ON follows (follower_id, following_id);
CREATE INDEX ix_follows_follower_id ON follows (follower_id);
CREATE INDEX ix_follows_following_id ON follows (following_id);