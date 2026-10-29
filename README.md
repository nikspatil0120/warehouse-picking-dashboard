# Warehouse Picking Dashboard

A full-stack Spring Boot 3.5 warehouse management system with MongoDB persistence, Spring Security RBAC, a premium dark/light UI, Docker containerisation, and a Jenkins CI/CD pipeline.

---

## Screenshots

| Dark Mode | Light Mode |
|-----------|------------|
| Glassmorphism sidebar, gradient mesh background, glowing status pills | Clean white cards, soft indigo accents, full contrast text |

---

## Features

- **Authentication** — Secure form-based login with Spring Security 6
- **Role-Based Access Control** — `WAREHOUSE_ADMIN` and `CUSTOMER` roles with method-level enforcement
- **Inventory Management** — Item catalogue with SKU search, stock levels, reorder thresholds
- **Order Processing** — Full order lifecycle (PENDING → PICKING → PACKED → SHIPPED → DELIVERED)
- **Stock Transactions** — Record inbound stock receipts; outbound managed automatically via orders
- **Stock Alerts** — Dashboard of items at or below reorder threshold
- **Customer Portal** — Customers can browse catalogue, place orders, view/cancel their own orders
- **Dark / Light Mode** — Persistent theme toggle on every page (saved to `localStorage`)
- **Docker** — Production-hardened image based on Tomcat 11.0 + JDK 21 Temurin
- **CI/CD** — Jenkins pipeline: build → test → Docker build → Docker Hub push

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Spring Boot 3.5.16 |
| Security | Spring Security 6 |
| Database | MongoDB (Atlas cloud / local) |
| UI | Thymeleaf, Inter font, custom CSS (glassmorphism) |
| Testing | JUnit 5, Mockito, Spring Security Test, Selenium, Testcontainers |
| Build | Maven |
| Container | Docker — `tomcat:11.0-jdk21-temurin-noble` |
| CI/CD | Jenkins declarative pipeline |

---

## Prerequisites

- Java 17+
- Maven 3.6+
- MongoDB Atlas account **or** a local MongoDB instance
- Docker (for container builds)
- Modern web browser

---

## Quick Start (local)

### 1. MongoDB

**Atlas (default — already configured in `application.properties`):**
No setup needed. The Atlas URI is pre-configured for `warehouse_dev`.

**Local MongoDB instead:**
```bash
docker run -d -p 27017:27017 --name mongodb mongo:7.0
```
Then update `src/main/resources/application.properties`:
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/warehouse
```

### 2. Run

```bash
mvn spring-boot:run
```

Open **http://localhost:8081**

### 3. Default credentials

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | WAREHOUSE_ADMIN |
| `customer1` | `customer123` | CUSTOMER |
| `customer2` | `customer123` | CUSTOMER |

> These are created automatically on first startup if no users exist in the database.

---

## Running Tests

```bash
# Full test suite (41 tests)
mvn clean test

# Individual suites
mvn test -Dtest=AuthenticationTests
mvn test -Dtest=AuthorizationTests
mvn test -Dtest=OrderServiceTests
mvn test -Dtest=ItemServiceTest
mvn test -Dtest=StockTransactionServiceTest
mvn test -Dtest=ItemCatalogueSeleniumTest
mvn test -Dtest=PickingDashboardApplicationTests
```

### Test breakdown (41 / 41 passing)

| Suite | Tests | Coverage |
|-------|-------|----------|
| `AuthenticationTests` | 8 | Login, logout, registration, invalid credentials |
| `AuthorizationTests` | 11 | Role-based endpoint access, redirect behaviour |
| `OrderServiceTests` | 10 | Order creation, status transitions, stock deduction |
| `ItemServiceTest` | 4 | CRUD, SKU uniqueness, validation |
| `StockTransactionServiceTest` | 4 | Stock-in recording, quantity updates |
| `ItemCatalogueSeleniumTest` | 3 | Browser-level catalogue rendering |
| `PickingDashboardApplicationTests` | 1 | Context loads |

---

## Docker

### Build image locally

```bash
mvn clean package -DskipTests
docker build -t picking-dashboard .
```

### Run container

```bash
docker run -p 8080:8080 \
  -e SPRING_DATA_MONGODB_URI="mongodb+srv://user:pass@cluster.mongodb.net/warehouse" \
  picking-dashboard
```

### Pull from Docker Hub

```bash
docker pull ryusseikun/picking-dashboard:build-24
```

### Healthcheck

The image exposes `/actuator/health` and includes a built-in Docker healthcheck:
```
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
```

---

## CI/CD Pipeline (Jenkins)

The `Jenkinsfile` defines a declarative pipeline with these stages:

```
Checkout → Build Package → Verify Application → Continuous Testing → Package Verification → Docker Build → Docker Hub Push
```

| Stage | What it does |
|-------|-------------|
| **Checkout** | Pulls from `main` branch |
| **Build Package** | `mvn clean package -DskipTests` |
| **Verify Application** | Confirms WAR artifact exists |
| **Continuous Testing** | `mvn clean test` — all 41 tests must pass |
| **Package Verification** | Re-confirms WAR post-test |
| **Docker Build** | Builds image tagged with `build-$BUILD_NUMBER` |
| **Docker Hub Push** | Pushes to `ryusseikun/picking-dashboard` |

Latest published image: `ryusseikun/picking-dashboard:build-24`

---

## Project Structure

```
src/
├── main/
│   ├── java/com/warehouse/picking/
│   │   ├── config/          # SecurityConfig, SampleDataLoader, SampleUserLoader, AdminBootstrap
│   │   ├── controller/      # REST & web controllers (Home, Items, Orders, Stock, Alerts, Customer portal, Login)
│   │   ├── model/           # Item, CustomerOrder, StockTransaction, User
│   │   ├── repository/      # MongoDB repositories
│   │   └── service/         # ItemService, OrderService, StockTransactionService, UserService
│   └── resources/
│       ├── application.properties
│       ├── static/
│       │   ├── css/style.css          # Full design system (dark + light themes)
│       │   └── js/theme.js            # Theme toggle with localStorage persistence
│       └── templates/
│           ├── home.html, login.html, register.html, orders.html, stock.html, alerts.html
│           ├── access-denied.html
│           ├── items/  (list.html, form.html)
│           └── customer/  (home, catalogue, order-form, my-orders, order-detail)
└── test/
    └── java/com/warehouse/picking/
        ├── AuthenticationTests.java
        ├── AuthorizationTests.java
        ├── OrderServiceTests.java
        ├── ItemServiceTest.java
        ├── StockTransactionServiceTest.java
        ├── ItemCatalogueSeleniumTest.java
        └── PickingDashboardApplicationTests.java
```

---

## API Endpoints

### Items
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/api/items` | WAREHOUSE_ADMIN |
| GET | `/api/items/{id}` | WAREHOUSE_ADMIN |
| POST | `/api/items` | WAREHOUSE_ADMIN |
| PUT | `/api/items/{id}` | WAREHOUSE_ADMIN |
| DELETE | `/api/items/{id}` | WAREHOUSE_ADMIN |

### Orders
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/api/orders` | WAREHOUSE_ADMIN |
| POST | `/api/orders` | WAREHOUSE_ADMIN |
| PUT | `/api/orders/{id}/status` | WAREHOUSE_ADMIN |

### Stock Transactions
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/api/stock-transactions` | WAREHOUSE_ADMIN |
| POST | `/api/stock-transactions` | WAREHOUSE_ADMIN |

---

## Web UI Routes

| Route | Description | Role |
|-------|-------------|------|
| `/` | Dashboard | Both |
| `/items` | Item catalogue | WAREHOUSE_ADMIN |
| `/items/new` | Add item form | WAREHOUSE_ADMIN |
| `/stock` | Stock transactions | WAREHOUSE_ADMIN |
| `/orders` | Order management | WAREHOUSE_ADMIN |
| `/alerts` | Stock alerts | WAREHOUSE_ADMIN |
| `/customer` | Customer home | CUSTOMER |
| `/customer/catalogue` | Browse products | CUSTOMER |
| `/customer/order-form` | Place order | CUSTOMER |
| `/customer/my-orders` | Order history | CUSTOMER |

---

## Security

- Spring Security 6 form-based authentication
- BCrypt password hashing
- CSRF disabled (stateless WAR deployment)
- Method-level `@PreAuthorize` on controllers
- Role prefix convention: `ROLE_WAREHOUSE_ADMIN`, `ROLE_CUSTOMER`
- Production admin provisioned via environment variables (`ADMIN_USERNAME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`) using `AdminBootstrap` — only active under `prod` profile

---

## Environment Variables (Production)

| Variable | Description |
|----------|-------------|
| `SPRING_DATA_MONGODB_URI` | MongoDB connection string |
| `ADMIN_USERNAME` | Initial admin username (prod profile only) |
| `ADMIN_EMAIL` | Initial admin email (prod profile only) |
| `ADMIN_PASSWORD` | Initial admin password (prod profile only) |

---

## License

MIT
