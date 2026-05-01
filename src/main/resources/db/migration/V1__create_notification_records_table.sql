-- Create notification_records table
CREATE TABLE notification_records (
    id            VARCHAR(36)   PRIMARY KEY,
    type          VARCHAR(20)   NOT NULL,
    recipient     VARCHAR(500)  NOT NULL,
    message       TEXT          NOT NULL,
    subject       VARCHAR(500),
    status        VARCHAR(10)   NOT NULL,
    error_message TEXT,
    timestamp     TIMESTAMP     NOT NULL
);

-- Create indexes for common query patterns
CREATE INDEX idx_notification_type      ON notification_records(type);
CREATE INDEX idx_notification_status    ON notification_records(status);
CREATE INDEX idx_notification_timestamp ON notification_records(timestamp DESC);
