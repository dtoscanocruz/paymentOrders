CREATE TABLE audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  request_id VARCHAR(128) NOT NULL,
  path VARCHAR(1024),
  method VARCHAR(16),
  username VARCHAR(255),
  status INT,
  error_message VARCHAR(2048),
  payload CLOB,
  started_at TIMESTAMP NULL,
  ended_at TIMESTAMP NULL,
  elapsed_ms BIGINT,
  trace_id VARCHAR(128),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_request_id ON audit_log(request_id);
