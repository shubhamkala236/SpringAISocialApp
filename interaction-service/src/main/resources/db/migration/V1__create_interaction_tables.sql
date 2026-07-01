CREATE TABLE post_likes (
                            id          UUID PRIMARY KEY,
                            post_id     UUID NOT NULL,
                            user_id     UUID NOT NULL,
                            username    VARCHAR(100) NOT NULL,
                            created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_post_likes_post_user ON post_likes (post_id, user_id);
CREATE INDEX ix_post_likes_post_id ON post_likes (post_id);
CREATE INDEX ix_post_likes_user_id ON post_likes (user_id);

CREATE TABLE saved_posts (
                             id               UUID PRIMARY KEY,
                             post_id          UUID NOT NULL,
                             user_id          UUID NOT NULL,
                             post_title       VARCHAR(200) NOT NULL,
                             post_content     VARCHAR(2000) NOT NULL,
                             post_username    VARCHAR(100) NOT NULL,
                             post_image_url   TEXT,
                             post_created_at  TIMESTAMPTZ NOT NULL,
                             saved_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_saved_posts_post_user ON saved_posts (post_id, user_id);
CREATE INDEX ix_saved_posts_user_id ON saved_posts (user_id);
CREATE INDEX ix_saved_posts_post_id ON saved_posts (post_id);