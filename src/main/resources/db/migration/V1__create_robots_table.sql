CREATE TABLE robots (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(50) UNIQUE NOT NULL,
    status        VARCHAR(20) NOT NULL,
    battery_level INTEGER DEFAULT 100,
    current_x     DOUBLE PRECISION DEFAULT 0,
    current_y     DOUBLE PRECISION DEFAULT 0,
    last_seen     TIMESTAMP,
    created_at    TIMESTAMP NOT NULL,
    updated_at    TIMESTAMP NOT NULL
);

