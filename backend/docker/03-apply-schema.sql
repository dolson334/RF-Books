-- Apply schema to test resort
-- This runs after init-db.sql

-- ============================================
-- Apply schema to testresort
-- ============================================
\echo 'Applying schema to testresort...'
SET search_path TO testresort, public;

-- Include the schema definition
\i /docker-scripts/schema.sql

\echo 'Schema applied to testresort successfully.'
