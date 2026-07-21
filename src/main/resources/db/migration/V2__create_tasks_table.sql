CREATE TABLE tasks (
    id                  BIGSERIAL       PRIMARY KEY,
    task_number         VARCHAR(20)     NOT NULL UNIQUE,
    pickup_location     VARCHAR(100)    NOT NULL,
    dropoff_location    VARCHAR(100)    NOT NULL,
    priority            VARCHAR(20)     NOT NULL,
    status              VARCHAR(20)     NOT NULL,
    robot_id            BIGINT          REFERENCES robots(id),
    estimated_distance  DOUBLE PRECISION,
    estimated_duration  INTEGER,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP       NOT NULL,
    completed_at        TIMESTAMP
);

CREATE INDEX idx_tasks_status      ON tasks(status);
CREATE INDEX idx_tasks_priority    ON tasks(priority);
CREATE INDEX idx_tasks_robot_id    ON tasks(robot_id);
CREATE INDEX idx_tasks_task_number ON tasks(task_number);
