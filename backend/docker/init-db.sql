-- Initialize RF Books database with default schema
-- This runs only on first container creation

-- Create default schema for development
CREATE SCHEMA IF NOT EXISTS client_default;

-- Create a demo client schema
CREATE SCHEMA IF NOT EXISTS client_demo;

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA client_default TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA client_demo TO postgres;

-- Set default search path
ALTER DATABASE rfbooks SET search_path TO client_default, public;
