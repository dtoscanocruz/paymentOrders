-- Cleanup script to ensure fresh tables on initialization
-- This script runs before `schema.sql` (alphabetical order) and will drop existing tables if present.
-- It is useful when recreating the DB volume to avoid duplicate-key errors from seed data.

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS payment_orders;
SET FOREIGN_KEY_CHECKS = 1;
