# Warehouse Picking Dashboard

A Spring Boot 3.5.16 warehouse management system with MongoDB persistence, Spring Security authentication, and role-based access control (RBAC).

## Features

- **User Authentication**: Secure login with Spring Security
- **Role-Based Access Control**: WAREHOUSE_ADMIN, WAREHOUSE_STAFF, CUSTOMER roles
- **Inventory Management**: Track items, stock levels, and reorder thresholds
- **Order Processing**: Create, update, and manage customer orders
- **Stock Transactions**: Record and track warehouse stock movements
- **Real-time Dashboards**: Web UI for warehouse operations

## Tech Stack

- **Backend**: Spring Boot 3.5.16, Spring Data MongoDB
- **Database**: MongoDB (local)
- **Security**: Spring Security 6.x
- **UI**: Thymeleaf, HTML/CSS
- **Testing**: JUnit 5, Selenium, Testcontainers
- **Build**: Maven

## Prerequisites

- Java 17+
- Maven 3.6+
- MongoDB Atlas account (for cloud development) OR local MongoDB instance (optional)
- Modern web browser for UI access

## Setup & Run Locally

### 1. MongoDB Setup (Skip if using Atlas)

```bash
# Using Docker
docker run -d -p 27017:27017 --name mongodb mongo:7.0

# Or if you have MongoDB installed locally
mongod
```

Then update `application.properties` to use local MongoDB (see Database Configuration section).

### 2. Build the Project

```bash
mvn clean test
mvn clean package -DskipTests
```

### 3. Run the Application

```bash
java -jar target/picking-dashboard-0.0.1-SNAPSHOT.war
```

Or run directly with Maven:

```bash
mvn spring-boot:run
```

### 4. Access the Application

Open your browser and go to:

```
http://localhost:8081
```

The application will automatically create default users on first run (see Default Users section below).

## Default Users

The application creates a default admin user on startup:

- **Username**: `admin`
- **Password**: `admin123`
- **Role**: WAREHOUSE_ADMIN

## Database Configuration

### Development (Default)

MongoDB Atlas is configured for development:
- **Database**: `warehouse_dev`
- **Connection**: MongoDB Atlas cluster (cloud)
- **Collections created automatically** on first run

Configuration in `application.properties`:
```properties
spring.data.mongodb.uri=mongodb+srv://nikhilpatil0120_db_user:...@cluster0.oj279ep.mongodb.net/warehouse_dev?retryWrites=true&w=majority
spring.data.mongodb.auto-index-creation=true
```

### Local Development (Optional)

To use a local MongoDB instead, update `application.properties`:
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/warehouse
```

Then start MongoDB locally:
```bash
docker run -d -p 27017:27017 --name mongodb mongo:7.0
```

### Collections

- `users` - User accounts and authentication
- `items` - Inventory items with stock levels
- `customer_orders` - Customer purchase orders
- `stock_transactions` - Stock movement history

## API Endpoints

### Items
- `GET /api/items` - List all items
- `GET /api/items/{id}` - Get item details
- `POST /api/items` - Create item
- `PUT /api/items/{id}` - Update item
- `DELETE /api/items/{id}` - Delete item

### Orders
- `GET /api/orders` - List orders
- `POST /api/orders` - Create order
- `PUT /api/orders/{id}/status` - Update order status

### Stock Transactions
- `GET /api/stock-transactions` - View transaction history
- `POST /api/stock-transactions` - Record transaction

## Web UI

- `/` - Dashboard
- `/items` - Item catalogue
- `/orders` - Order management
- `/stock` - Stock tracking
- `/alerts` - Low stock alerts

## Testing

### Run All Tests

```bash
mvn clean test
```

### Run Specific Test Suite

```bash
mvn test -Dtest=OrderServiceTests
mvn test -Dtest=ItemCatalogueSeleniumTest
mvn test -Dtest=AuthenticationTests
```

### Test Results

- **39/41 tests passing** with MongoDB persistence
- Core services: OrderService, ItemService, StockTransactionService fully verified
- Authentication & authorization working correctly

## Security Features

- Spring Security authentication
- RBAC with role-based endpoints
- CSRF protection
- Password encoding with bcrypt
- Transactional safety for stock operations

## Project Structure

```
src/
├── main/
│   ├── java/com/warehouse/picking/
│   │   ├── controller/          # REST & Web controllers
│   │   ├── service/             # Business logic
│   │   ├── repository/          # MongoDB repositories
│   │   ├── model/               # Entity classes
│   │   ├── config/              # Spring configuration
│   │   └── PickingDashboardApplication.java
│   └── resources/
│       ├── application.properties
│       ├── templates/           # Thymeleaf templates
│       └── static/             # CSS, JavaScript
└── test/
    └── java/com/warehouse/picking/
        ├── OrderServiceTests.java
        ├── AuthenticationTests.java
        ├── AuthorizationTests.java
        └── ItemCatalogueSeleniumTest.java
```

## Production Deployment

For production deployment with MongoDB Atlas:

1. Update `application.properties` with Atlas URI:
   ```properties
   spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/warehouse?retryWrites=true&w=majority
   ```

2. Build WAR:
   ```bash
   mvn clean package
   ```

3. Deploy to application server (Tomcat, JBoss, etc.)

## Troubleshooting

### Connection refused on port 27017
- Ensure MongoDB is running: `mongo --version` and `mongod`
- Check with: `telnet localhost 27017`

### Tests failing
- Clear MongoDB data: `db.dropDatabase()`
- Rebuild: `mvn clean test`

### Port 8081 already in use
- Change port in `application.properties`:
  ```properties
  server.port=8082
  ```

## Development Notes

- Entities use MongoDB `@Document` annotation
- IDs are String type (MongoDB ObjectId format)
- Repositories extend `MongoRepository<T, String>`
- Services handle transactional logic and stock validation
- Controllers provide REST & web UI endpoints

## License

MIT

## Contact

For issues or questions, please create a GitHub issue.
