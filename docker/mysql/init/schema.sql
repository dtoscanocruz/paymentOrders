CREATE TABLE IF NOT EXISTS payment_orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  external_id VARCHAR(255) UNIQUE,
  debtor_iban VARCHAR(255),
  creditor_iban VARCHAR(255),
  amount DECIMAL(19,2),
  currency VARCHAR(10),
  remittance_info VARCHAR(1024),
  requested_execution_date DATE,
  status VARCHAR(50),
  last_update TIMESTAMP
);

-- Audit log table for request/response auditing (reactive, R2DBC, MySQL)
CREATE TABLE IF NOT EXISTS audit_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  request_id VARCHAR(128) NOT NULL,
  path VARCHAR(1024),
  method VARCHAR(16),
  username VARCHAR(255),
  status INT,
  error_message VARCHAR(2048),
  payload TEXT,
  started_at TIMESTAMP(3) NULL,
  ended_at TIMESTAMP(3) NULL,
  elapsed_ms BIGINT,
  trace_id VARCHAR(128),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_request_id (request_id),
  INDEX idx_created_at (created_at),
  INDEX idx_path (path(255))
);
