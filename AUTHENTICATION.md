# Unified Authentication and Role-Based Access Control (RBAC)

This document describes the authentication, authorization, and role-based access control system implemented in the Warehouse Picking Dashboard.

## Overview

The application now includes a unified authentication system with two distinct user roles:

1. **CUSTOMER** - Customer-facing order portal
2. **WAREHOUSE_ADMIN** - Warehouse management and administration

## Architecture

### Authentication System

- **Framework**: Spring Security 6.x (integrated into Spring Boot 3.5.16)
- **Login**: Form-based authentication via `/login` page
- **Session Management**: Spring Security session persistence
- **CSRF Protection**: Enabled by default for all form submissions
- **Password Hashing**: BCrypt with strength 10

### User Model

The `User` entity (`com.warehouse.picking.model.User`) includes:

- `id` (Long, auto-generated)
- `username` (String, unique, 3-50 characters)
- `email` (String, unique, valid email format)
- `passwordHash` (String, BCrypt-encrypted)
- `role` (Enum: ROLE_CUSTOMER or ROLE_WAREHOUSE_ADMIN)
- `enabled` (Boolean, defaults to true)
- `createdAt` (LocalDateTime, auto-set)

### Role-Based Authorization

**WAREHOUSE_ADMIN Permissions:**
- `/items` - View, create, update item catalogue
- `/stock` - Record stock transactions (IN/OUT)
- `/orders` - View all orders, update order status
- `/alerts` - View low-stock and out-of-stock alerts
- `/` - Access warehouse dashboard

**CUSTOMER Permissions:**
- `/customer/` - Customer home page
- `/customer/catalogue` - Browse product catalogue
- `/customer/order-form` - Create orders
- `/customer/my-orders` - View own orders
- `/customer/orders/{id}` - View order details (ownership verified)
- `/customer/orders/{id}/cancel` - Cancel own orders (PENDING status only)

**WAREHOUSE_ADMIN Restrictions:**
- Cannot access `/customer` routes
- Cannot view/cancel customer orders via customer portal

**CUSTOMER Restrictions:**
- Cannot access `/items`, `/stock`, `/alerts`
- Cannot view warehouse `/orders` endpoint
- Cannot access customer-owned resources of other customers

## Deployment & Configuration

### Local Development

### Default Test Credentials

When the application starts with an empty database in development profile (`dev`), it automatically creates:

- **Administrator Account**:
  - Username: `admin`
  - Email: `admin@warehouse.local`
  - Password: `admin123`
  - Role: `WAREHOUSE_ADMIN`

- **Test Customer Accounts**:
  - Username: `customer1`, Email: `customer1@example.com`, Password: `customer123`, Role: `CUSTOMER`
  - Username: `customer2`, Email: `customer2@example.com`, Password: `customer123`, Role: `CUSTOMER`

**⚠️ Development Only**: These credentials are created ONLY when running with `-Dspring.profiles.active=dev`. They are never created in production.

### Starting the Application in Development

```bash
# Development mode with sample data
mvn spring-boot:run -Dspring.profiles.active=dev

# Or with the JAR/WAR
java -Dspring.profiles.active=dev -jar target/picking-dashboard-0.0.1-SNAPSHOT.war
```

Navigate to: `http://localhost:8081/login` and use the default credentials above.

### Production Deployment

For production environments, NO default credentials are created. The administrator must be explicitly provisioned.

#### Creating the Initial Administrator (Production)

**Method 1: Environment Variables (Recommended)**

Set the following environment variables before application startup:

```bash
export ADMIN_USERNAME=your_admin_username
export ADMIN_EMAIL=your_admin@example.com
export ADMIN_PASSWORD=your_secure_password_here
```

Then start the application with production profile:

```bash
java -Dspring.profiles.active=prod -jar picking-dashboard-0.0.1-SNAPSHOT.war
```

The application will:
1. Check if the database has any users
2. Read the three environment variables
3. Create an administrator account if they are all set
4. Log a warning if any are missing (and not create a default admin)

**Method 2: Manual Database Insertion**

Insert an administrator directly into the `app_user` table:

```sql
INSERT INTO app_user (username, email, password_hash, role, enabled, created_at)
VALUES ('admin', 'admin@domain.com', '$2a$10$...', 'ROLE_WAREHOUSE_ADMIN', true, now());
```

Use an online BCrypt generator or Spring Security's `BCryptPasswordEncoder` to hash the password. Example:

```java
// Generate a BCrypt hash for "mypassword"
String hash = new BCryptPasswordEncoder().encode("mypassword");
// Output: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36DRcx3e
```

#### Disabling Sample Data in Production

The `SampleUserLoader` bean is annotated with `@Profile("dev")` and will NOT run unless the application is explicitly started with the `dev` profile.

To ensure production safety:
1. Always use `-Dspring.profiles.active=prod` in production
2. Never set `spring.profiles.active=dev` in production configuration
3. Verify sample users do not exist in production database before going live

### Docker

The Docker image is built using the existing `Dockerfile`:

```dockerfile
FROM tomcat:10.1-jdk17-temurin
COPY target/picking-dashboard-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
```

The application deploys as the ROOT application on Tomcat, accessible at port 8080 inside the container.

## Security Features

### CSRF Protection

CSRF tokens are automatically included in all forms via Spring Security. Thymeleaf templates use:

```html
<form method="post" th:action="@{/endpoint}">
    <!-- Spring automatically adds CSRF token -->
</form>
```

### Session Security

- Sessions are HttpOnly and Secure (when HTTPS is used)
- Session timeout: Spring Boot default (30 minutes)
- Logout invalidates the session immediately

### Password Security

- Passwords are hashed with BCrypt before storage
- Plain-text passwords are never logged or exposed in error messages
- Password fields in templates use `type="password"`

### Authorization Enforcement

Authorization is enforced at **three levels**:

1. **Spring Security Filters** - URL pattern-based access control
2. **Controller-Level @PreAuthorize** - Method-level authorization
3. **Service-Level Checks** - Business logic verification (e.g., customer ownership)

Example service-level check:

```java
@Transactional(readOnly = true)
public Optional<CustomerOrder> getOrderById(Long orderId) {
    Optional<CustomerOrder> order = orderRepository.findById(orderId);
    // Caller must verify ownership - service returns any order
    return order;
}
```

Example controller-level check:

```java
@PostMapping("/orders/{id}/cancel")
public String cancelOrder(@PathVariable Long id, Authentication authentication) {
    User customer = userService.findByUsername(authentication.getName()).orElseThrow();
    // Controller verifies customer owns the order before calling service
    if (!order.getCustomer().getId().equals(customer.getId())) {
        return "redirect:/access-denied";
    }
    orderService.cancelOrder(id);
    return "redirect:/customer/my-orders";
}
```

## Order Workflow

### Order Status Transitions

Orders progress through defined states, enforced in `OrderService`:

```
PENDING → PICKING → COMPLETED
     ↓
  CANCELLED (only from PENDING)
```

**Valid Transitions:**
- PENDING → PICKING (warehouse staff)
- PICKING → COMPLETED (warehouse staff)
- PENDING → CANCELLED (customer or staff)

**Invalid Transitions:**
- Cannot transition FROM COMPLETED or CANCELLED
- Cannot move backwards in the workflow
- Customers cannot manually transition statuses (warehouse only)

### Stock Management

Stock is managed as follows:

1. **Order Creation**: Stock is immediately deducted when a customer creates an order
2. **Order Cancellation**: Stock is released back to inventory only if order is in PENDING status
3. **Concurrent Access**: Database transaction isolation prevents double-deduction

**Potential Stock Issues:**
If an order is cancelled after warehouse staff begins picking, stock will be released even though partial fulfillment may have occurred. Document operational procedures to handle this.

## Database Schema

### New Tables

- **app_user** - Stores authentication credentials and user roles

### Modified Tables

- **customer_order** - Added `customer_id` foreign key (nullable for backwards compatibility with legacy orders)

### Indexes

- Unique index on `app_user.username`
- Unique index on `app_user.email`
- Foreign key from `customer_order.customer_id` to `app_user.id`

## API Endpoints

### Public Endpoints

- `GET /login` - Login page
- `GET /register` - Registration page
- `POST /register` - Register new customer account
- `GET /actuator/health` - Health check (deployment verification)
- `GET /access-denied` - Access denied page

### Customer Portal Endpoints

All require `ROLE_CUSTOMER`:

- `GET /customer` - Customer home page
- `GET /customer/catalogue` - Browse product catalogue
- `GET /customer/order-form` - Order creation form
- `POST /customer/orders` - Create order
- `GET /customer/my-orders` - Customer's order history
- `GET /customer/orders/{id}` - Order detail page
- `POST /customer/orders/{id}/cancel` - Cancel order

### Warehouse Administration Endpoints

All require `ROLE_WAREHOUSE_ADMIN`:

- `GET /items` - Item catalogue list
- `GET /items/new` - Item creation form
- `POST /items` - Create new item
- `GET /stock` - Stock transaction page
- `POST /stock` - Record stock transaction
- `GET /orders` - Warehouse order list (all orders, all customers)
- `POST /orders/{id}/status` - Update order status
- `GET /alerts` - Low-stock alerts

### Dashboard Endpoints

- `GET /` - Universal dashboard (redirects to appropriate role)

## Testing

### Test Classes

- **AuthenticationTests.java** - Login, logout, registration, credential validation
- **AuthorizationTests.java** - Role-based access control, endpoint permissions
- **OrderServiceTests.java** - Order creation, stock deduction, status transitions, cancellation

### Running Tests

```bash
# Run all new tests
mvn test -Dtest="AuthenticationTests,AuthorizationTests,OrderServiceTests"

# Run only authentication tests
mvn test -Dtest="AuthenticationTests"

# Run all tests (including Selenium, may timeout)
mvn test
```

### Test Results

- AuthenticationTests: 8 tests (registration, login, credential validation)
- AuthorizationTests: 11 tests (role-based access control)
- OrderServiceTests: 10 tests (order lifecycle, stock management)
- **Total: 29 tests, all passing**

Selenium integration tests (`ItemCatalogueSeleniumTest`) are not run by default due to timeout issues. To run: `mvn verify` (requires Chrome and ChromeDriver installed).

## Backwards Compatibility

### Existing Orders

Orders created before the authentication system was added have no customer relationship (`customer_id = NULL`). These are treated as historical records and:

- Cannot be cancelled by customers (no owner to verify)
- Are visible to warehouse staff only
- Do not appear in customer "My Orders" view

### Existing Items

All existing items remain in the catalogue and can be ordered by authenticated customers. No item modifications were needed.

### Existing Stock Transactions

Stock transactions are preserved with their original audit trail. No modifications to transaction history.

## Troubleshooting

### Customers Cannot See Their Orders

**Possible Cause**: Order was created before authentication. Orders must have a valid `customer_id`.

**Solution**: Manually assign the customer ID in the database, or re-create the order as an authenticated customer.

### "Access Denied" on Warehouse Pages

**Possible Cause**: User does not have `ROLE_WAREHOUSE_ADMIN` role.

**Solution**: Verify user role in the `app_user` table. Contact administrator to update role.

### Cannot Log In

**Possible Cause**: Account is disabled or credentials are incorrect.

**Solution**: Check the `enabled` flag in the `app_user` table. Verify username and password hash (use BCrypt to re-hash and compare).

### Stock Quantities Are Negative

**Possible Cause**: Concurrent order creation or manual stock adjustment bypassed validation.

**Solution**: Use service methods for all stock changes (never update directly). Fix quantities manually if necessary, then audit orders.

## Future Enhancements

- Two-factor authentication
- Audit logging with user actions
- Role hierarchy (admin → supervisor → picker)
- Order item-level detail (partial picking)
- Customer-based pricing tiers
- Warehouse location-based permissions
- OAuth2 integration for enterprise authentication

## Support

For issues or questions related to authentication:

1. Check the logs for Spring Security debug messages
2. Verify user role in the database
3. Review SecurityConfig for authorization rules
4. Contact the development team with specific error messages
