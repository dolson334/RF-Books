# Onboarding Backend API Specification

## Base URL
`http://localhost:8081/api/onboarding`

---

## Chart of Accounts Endpoints

### 1. Save Chart of Accounts
**POST** `/onboarding/chart-of-accounts`

Saves the user's chart of accounts setup.

**Request Body:**
```json
[
  {
    "accountNumber": "1000",
    "accountName": "Cash",
    "accountType": "ASSET",
    "description": "Checking and savings accounts"
  },
  {
    "accountNumber": "4000",
    "accountName": "Revenue",
    "accountType": "REVENUE",
    "description": "Income from sales"
  }
]
```

**Response:** 204 No Content

---

### 2. Get Chart of Accounts
**GET** `/onboarding/chart-of-accounts`

Retrieves the user's chart of accounts.

**Response:**
```json
[
  {
    "id": 1,
    "accountNumber": "1000",
    "accountName": "Cash",
    "accountType": "ASSET",
    "description": "Checking and savings accounts"
  },
  {
    "id": 2,
    "accountNumber": "4000",
    "accountName": "Revenue",
    "accountType": "REVENUE",
    "description": "Income from sales"
  }
]
```

---

## Products & Services Endpoints

### 1. Save Products & Services
**POST** `/onboarding/products-services`

Saves the user's products and services.

**Request Body:**
```json
[
  {
    "name": "RV Site Rental",
    "type": "SERVICE",
    "defaultPrice": 45.00,
    "unitOfMeasure": "night",
    "description": "Standard RV site with hookups"
  },
  {
    "name": "Firewood Bundle",
    "type": "PRODUCT",
    "defaultPrice": 8.00,
    "unitOfMeasure": "bundle",
    "description": "Seasoned firewood"
  }
]
```

**Response:** 204 No Content

---

### 2. Get Products & Services
**GET** `/onboarding/products-services`

Retrieves the user's products and services.

**Response:**
```json
[
  {
    "id": 1,
    "name": "RV Site Rental",
    "type": "SERVICE",
    "defaultPrice": 45.00,
    "unitOfMeasure": "night",
    "description": "Standard RV site with hookups",
    "revenueAccountId": 2
  },
  {
    "id": 2,
    "name": "Firewood Bundle",
    "type": "PRODUCT",
    "defaultPrice": 8.00,
    "unitOfMeasure": "bundle",
    "description": "Seasoned firewood",
    "revenueAccountId": 2
  }
]
```

---

## Progress Endpoints

### 1. Get Onboarding Progress
**GET** `/onboarding/progress`

Returns the current onboarding progress status.

**Response:**
```json
{
  "bankConnected": true,
  "chartOfAccountsCreated": true,
  "productsServicesCreated": false,
  "completed": false
}
```

---

### 2. Complete Onboarding
**POST** `/onboarding/complete`

Marks the onboarding process as complete.

**Request Body:** `{}`

**Response:** 204 No Content

---

## Data Models

### ChartOfAccount
```java
{
  "id": Long,
  "accountNumber": String,
  "accountName": String,
  "accountType": "ASSET" | "LIABILITY" | "EQUITY" | "REVENUE" | "EXPENSE",
  "description": String (optional)
}
```

### ProductService
```java
{
  "id": Long,
  "name": String,
  "type": "PRODUCT" | "SERVICE",
  "defaultPrice": Double,
  "unitOfMeasure": String,
  "description": String (optional),
  "revenueAccountId": Long (optional)
}
```

### OnboardingProgress
```java
{
  "bankConnected": Boolean,
  "chartOfAccountsCreated": Boolean,
  "productsServicesCreated": Boolean,
  "completed": Boolean
}
```

---

## Java Backend Implementation

Create these files in `com.rfbooks.backend.onboarding`:

### Entity Classes

**ChartOfAccount.java**
```java
@Entity
@Table(name = "chart_of_accounts")
public class ChartOfAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId = "default-user";
    
    @Column(nullable = false, unique = true)
    private String accountNumber;
    
    @Column(nullable = false)
    private String accountName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;
    
    private String description;
    
    // Getters and setters
}

enum AccountType {
    ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE
}
```

**ProductService.java**
```java
@Entity
@Table(name = "products_services")
public class ProductService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId = "default-user";
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType type;
    
    private Double defaultPrice;
    private String unitOfMeasure;
    private String description;
    private Long revenueAccountId;
    
    // Getters and setters
}

enum ItemType {
    PRODUCT, SERVICE
}
```

**OnboardingProgress.java**
```java
@Entity
@Table(name = "onboarding_progress")
public class OnboardingProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String userId = "default-user";
    
    private Boolean bankConnected = false;
    private Boolean chartOfAccountsCreated = false;
    private Boolean productsServicesCreated = false;
    private Boolean completed = false;
    
    // Getters and setters
}
```

---

## CORS Configuration

Update your CORS to include the new endpoints:

```java
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4201"})
```
