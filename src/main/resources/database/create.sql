CREATE TABLE orders (
                        id             VARCHAR(36)  PRIMARY KEY,
                        product        VARCHAR(255) NOT NULL,
                        quantity       INTEGER      NOT NULL,
                        status         VARCHAR(20)  NOT NULL,
                        attachment_key VARCHAR(512),
                        created_at     TIMESTAMP    NOT NULL DEFAULT now()
);