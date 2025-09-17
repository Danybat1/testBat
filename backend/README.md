# FreightOps Backend

A comprehensive freight management system built with Spring Boot 3.2, designed to handle LTA (Less Than Truckload) shipments, client management, and invoicing.

## 🚀 Features

- **LTA Management**: Create, track, and manage freight shipments
- **Client Management**: Manage client information and relationships
- **Invoice Management**: Generate and track invoices
- **JWT Authentication**: Secure API with role-based access control
- **Real-time Tracking**: Track shipment status and location updates
- **RESTful APIs**: Comprehensive REST API endpoints
- **Database Migration**: Flyway-based database versioning
- **Containerized Deployment**: Docker and Docker Compose support

## 🏗️ Architecture

### Tech Stack
- **Java 17+**
- **Spring Boot 3.2**
- **Spring Security** with JWT authentication
- **Spring Data JPA** with Hibernate
- **PostgreSQL** database
- **Redis** for caching
- **RabbitMQ** for messaging
- **MinIO** for object storage
- **Flyway** for database migrations
- **Docker** for containerization

### Modular Structure
```
src/main/java/com/freightops/
├── api/                    # REST Controllers
├── config/                 # Configuration classes
├── domain/                 # Domain entities
│   ├── client/            # Client domain
│   ├── invoice/           # Invoice domain
│   ├── lta/              # LTA domain
│   └── tracking/         # Tracking domain
├── dto/                   # Data Transfer Objects
├── repository/            # Data Access Layer
└── service/              # Business Logic Layer
```

## 🔐 Security & Authentication

### User Roles
- **ADMIN**: Full system access
- **AGENT**: LTA and client management
- **FINANCE**: Invoice and financial data access

### Default Users (Development)
- `admin` / `admin123` (ADMIN role)
- `agent` / `agent123` (AGENT role)
- `finance` / `finance123` (FINANCE role)

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose (optional)

### Local Development

1. **Clone and navigate to the project:**
   ```bash
   cd backend
   ```

2. **Start infrastructure services:**
   ```bash
   docker-compose up -d postgres redis rabbitmq minio
   ```

3. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the application:**
   - API: http://localhost:8080
   - Health Check: http://localhost:8080/actuator/health

### Full Docker Deployment

```bash
# Start all services including the backend
docker-compose --profile full-stack up -d

# Or with additional tools (pgAdmin)
docker-compose --profile full-stack --profile tools up -d
```

## 📚 API Documentation

### Authentication Endpoints
- `POST /api/auth/login` - User authentication
- `POST /api/auth/validate` - Token validation
- `GET /api/auth/user` - Current user info

### LTA Endpoints
- `POST /api/lta` - Create new LTA
- `GET /api/lta` - List LTAs with pagination and filtering
- `GET /api/lta/{id}` - Get LTA by ID
- `GET /api/lta/number/{ltaNumber}` - Get LTA by number
- `GET /api/lta/tracking/{trackingNumber}` - Public tracking (no auth)
- `PUT /api/lta/{id}` - Update LTA
- `PATCH /api/lta/{id}/status` - Update LTA status
- `DELETE /api/lta/{id}` - Delete LTA
- `GET /api/lta/stats` - LTA statistics

### Example API Usage

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

**Create LTA:**
```bash
curl -X POST http://localhost:8080/api/lta \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "shipper": "Acme Corp",
    "consignee": "Global Logistics",
    "weight": 1500.00,
    "description": "Electronics shipment",
    "origin": "New York, NY",
    "destination": "Los Angeles, CA",
    "declaredValue": 20000.00
  }'
```

**Track Shipment (Public):**
```bash
curl http://localhost:8080/api/lta/tracking/TRK-20240001
```

## 🗄️ Database Schema

### Main Tables
- **client**: Client information and contact details
- **lta**: LTA shipment records
- **invoice**: Invoice records linked to clients
- **lta_tracking**: Tracking history for shipments

### Key Features
- Optimistic locking with `@Version`
- Audit timestamps (created_at, updated_at)
- Comprehensive indexing for performance
- Foreign key constraints for data integrity

## 🔧 Configuration

### Environment Variables (Production)
```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/freightops
DATABASE_USERNAME=freightops_user
DATABASE_PASSWORD=your_password
REDIS_HOST=localhost
RABBITMQ_HOST=localhost
MINIO_ENDPOINT=http://localhost:9000
JWT_SECRET=your-secret-key
```

### Application Profiles
- `dev`: Development with debug logging
- `test`: Testing with H2 in-memory database
- `prod`: Production with environment variables

## 🐳 Docker Services

### Infrastructure Services
- **PostgreSQL**: Main database (port 5432)
- **Redis**: Caching layer (port 6379)
- **RabbitMQ**: Message broker (port 5672, management UI: 15672)
- **MinIO**: Object storage (port 9000, console: 9001)

### Management Tools
- **pgAdmin**: Database management (port 5050) - optional

## 🔍 Monitoring & Health Checks

- **Health Check**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Info**: `/actuator/info`

## 🧪 Testing

```bash
# Run unit tests
./mvnw test

# Run integration tests
./mvnw verify

# Run with specific profile
./mvnw test -Dspring.profiles.active=test
```

## 📝 Development Notes

### Code Quality
- Clean architecture with separation of concerns
- Comprehensive validation and error handling
- Optimistic locking for concurrent updates
- Audit trails for all entities

### Security Best Practices
- JWT tokens with configurable expiration
- Role-based access control
- CORS configuration
- Input validation and sanitization

### Performance Optimizations
- Database indexing strategy
- Connection pooling with HikariCP
- Lazy loading for entity relationships
- Pagination for large datasets

## 🤝 Contributing

1. Follow the existing code structure and naming conventions
2. Add appropriate tests for new features
3. Update documentation for API changes
4. Use meaningful commit messages

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For issues and questions:
1. Check the application logs: `logs/freightops.log`
2. Verify database connectivity and migrations
3. Ensure all required environment variables are set
4. Check Docker container health status
