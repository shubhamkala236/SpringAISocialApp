CREATE TABLE posts (
                       id                 UUID PRIMARY KEY,
                       user_id            UUID NOT NULL,
                       username           VARCHAR(100) NOT NULL,
                       user_avatar_url    TEXT,
                       title              VARCHAR(200) NOT NULL,
                       content            VARCHAR(2000) NOT NULL,
                       image_url          TEXT,
                       image_public_id    TEXT,
                       created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at         TIMESTAMPTZ
);

CREATE INDEX ix_posts_user_id ON posts (user_id);
CREATE INDEX ix_posts_created_at ON posts (created_at DESC);