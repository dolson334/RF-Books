# RF Books - Multi-Tenant Setup

## Overview
RF Books uses **schema-based multitenancy** where each client gets their own PostgreSQL schema.

## Docker Setup

### Start Database
```bash
cd backend
docker-compose up -d
```

### Stop Database
```bash
docker-compose down
```

### View Logs
```bash
docker-compose logs -f postgres
```

## Tenant Management

### Default Tenant
- Schema: `client_default`
- Used when no `X-Tenant-ID` header is provided

### Add New Tenant
Connect to PostgreSQL and create a new schema:

```bash
docker exec -it rfbooks-postgres psql -U postgres -d rfbooks
```

Then in psql:
```sql
CREATE SCHEMA client_newclient;
GRANT ALL PRIVILEGES ON SCHEMA client_newclient TO postgres;
```

### API Usage
Include tenant ID in request header:

```bash
curl -H "X-Tenant-ID: client_demo" http://localhost:8081/api/onboarding/progress
```

### Frontend Integration
Update Angular HTTP interceptor to add tenant header:

```typescript
// In your HTTP interceptor
headers: {
  'X-Tenant-ID': 'client_demo'  // Get from user context
}
```

## Schema Structure
Each tenant schema contains identical tables:
- `onboarding_progress`
- `chart_of_accounts`
- `products_services`
- `plaid_connections`
- `reconciliation_runs`

## Database Connection
- **Host:** localhost:5433
- **Database:** rfbooks
- **User:** postgres
- **Password:** password

## Data Isolation
- Each client's data is completely isolated in their own schema
- No cross-tenant data access
- Simple backup/restore per client

## Testing
For development, use the default tenant or create test schemas:
```sql
CREATE SCHEMA client_test1;
CREATE SCHEMA client_test2;
```
