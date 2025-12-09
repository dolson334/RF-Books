-- Initialize RF Books database with default schema
-- This runs only on first container creation

-- Create test resort schema for demo
CREATE SCHEMA IF NOT EXISTS testresort;

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA testresort TO postgres;

-- Set default search path
ALTER DATABASE rfbooks SET search_path TO testresort, public;
