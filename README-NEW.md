# OCS Compliance Dashboard - Spring Boot + React

A comprehensive enterprise-grade web application for monitoring server compliance across multiple cloud regions. Built with **Spring Boot** backend and **React** frontend, packaged as a **single executable JAR**.

## 🚀 Features

### Backend (Spring Boot)
- **RESTful API** with Spring Boot 3.2
- **Vault Integration** using Spring Vault with AppRole authentication
- **Multi-Region Support** across Paris and North regions
- **Reactive HTTP Client** with WebClient for async operations
- **Retry Logic** with configurable timeouts and exponential backoff
- **Health Checks** via Spring Actuator
- **Comprehensive Logging** with SLF4J

### Frontend (React)
- **Modern SPA** built with React 18 and Vite
- **Real-time Dashboard** with KPI cards and statistics
- **Interactive Charts** using Chart.js
- **Advanced Filtering** and search capabilities
- **Auto-refresh** functionality
- **Responsive Design** with Tailwind CSS
- **Dark Mode** support

### Build & Deployment
- **Single JAR** packaging with embedded Tomcat
- **Automated Build** using Gradle
- **Frontend Integration** - React automatically built and bundled
- **Production Ready** with optimized builds

## 📋 Prerequisites

- **Java 17+** (JDK 17 or higher)
- **Gradle 8+** (or use included wrapper)
- **Node.js 18+** (automatically downloaded by Gradle)
- **HashiCorp Vault** access with AppRole credentials
- **Network access** to OCS APIs

## 🏗️ Architecture

```
compliance-dashboard/
├── src/
│   ├── main/
│   │   ├── java/com/compliance/dashboard/
│   │   │   ├── ComplianceDashboardApplication.java
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── service/         # Business logic
│   │   │   ├── client/          # External API clients
│   │   │   └── model/           # Data models
│   │   └── resources/
│   │       ├── application.yml  # Application config
│   │       └── static/          # React build (auto-generated)
│   └── test/                    # Unit tests
├── frontend/                    # React application
├── build.gradle                 # Gradle build configuration
└── settings.gradle
```

## 🔧 Installation & Setup

### 1. Clone the Repository

```bash
cd ~/Desktop
git clone <repository-url>
cd compliance-dashboard
```

### 2. Configure Environment Variables

Create a `.env` file in the project root:

```bash
cp .env.example .env
```

Edit `.env` with your credentials:

```bash
VAULT_ADDR=https://your-vault-server.com
VAULT_NAMESPACE=your-namespace
VAULT_ROLE_ID=your-role-id-here
VAULT_SECRET_ID=your-secret-id-here
VAULT_CONFIG_PATH=compliance/config
PORT=5000
LOG_LEVEL=INFO
```

### 3. Configure Vault

Ensure your Vault contains the application configuration at the specified path.

**Example Vault Configuration:**

```json
{
  "LSG": {
    "account_id": "your-account-id",
    "client_id": "your-client-id",
    "client_secret": "your-client-secret",
    "iamaas_url": "https://iamaas.example.com/oauth/token",
    "sgcp_iamaas_scopes": "ocs:read ccs:read"
  },
  "APP2": {
    "account_id": "another-account-id",
    "client_id": "another-client-id",
    "client_secret": "another-client-secret",
    "iamaas_url": "https://iamaas.example.com/oauth/token",
    "sgcp_iamaas_scopes": "ocs:read ccs:read"
  }
}
```

## 🚀 Building the Application

### Build Everything (Backend + Frontend)

```bash
# Using Gradle wrapper (recommended)
./gradlew clean build

# Or using installed Gradle
gradle clean build
```

This single command will:
1. Download Node.js and npm (if needed)
2. Install frontend dependencies (`npm install`)
3. Build React frontend (`npm run build`)
4. Copy frontend build to Spring Boot static resources
5. Compile Java code
6. Run tests
7. Package everything as `compliance-dashboard.jar`

**Build Output:** `build/libs/compliance-dashboard.jar`

### Build Options

```bash
# Build without tests
./gradlew clean build -x test

# Build only backend (skip frontend)
./gradlew clean bootJar -x buildFrontend

# Build only frontend
./gradlew buildFrontend

# Clean all builds
./gradlew clean
```

## 🏃 Running the Application

### Run the JAR (Production)

```bash
# Set environment variables
export VAULT_ADDR=https://vault.example.com
export VAULT_NAMESPACE=your-namespace
export VAULT_ROLE_ID=your-role-id
export VAULT_SECRET_ID=your-secret-id
export PORT=5000

# Run the application
java -jar build/libs/compliance-dashboard.jar
```

### Run with Environment File

```bash
# Load environment variables from .env
export $(cat .env | xargs)

# Run the application
java -jar build/libs/compliance-dashboard.jar
```

### Run in Development Mode

```bash
# Run with Gradle (auto-reload on code changes)
./gradlew bootRun
```

### Access the Application

Once running, access the application at:
- **Frontend:** http://localhost:5000
- **API:** http://localhost:5000/api
- **Health Check:** http://localhost:5000/api/health
- **Actuator:** http://localhost:5000/actuator/health

## 📡 API Endpoints

### Health Check
```bash
GET /api/health
```
Returns API health status and Vault connectivity.

**Response:**
```json
{
  "status": "healthy",
  "vault_connected": true,
  "message": "API is running and Vault is accessible"
}
```

### Get Available Apps
```bash
GET /api/apps
```
Returns list of applications configured in Vault.

**Response:**
```json
{
  "apps": ["LSG", "APP2"],
  "count": 2
}
```

### Get Compliance Data
```bash
GET /api/compliance?app=<app_name>&debug=<true|false>
```
- `app` (optional): Specific app to check. If omitted, checks all apps.
- `debug` (optional): Enable debug logging.

**Response:**
```json
{
  "app_name": "LSG",
  "timestamp": "2025-01-15T10:30:00",
  "current_week": 3,
  "current_year": 2025,
  "regions": {
    "paris": {
      "total_servers": 50,
      "compliant": 45,
      "non_compliant": 5,
      "compliance_percentage": 90.0,
      "good_servers": [...],
      "bad_servers": [...]
    }
  }
}
```

### Get Compliance Summary
```bash
GET /api/compliance/summary?debug=<true|false>
```
Returns aggregated compliance statistics across all apps and regions.

## 🧪 Testing

### Run Tests

```bash
# Run all tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport

# Run specific test
./gradlew test --tests ComplianceServiceTest
```

### Manual API Testing

```bash
# Health check
curl http://localhost:5000/api/health

# Get apps
curl http://localhost:5000/api/apps

# Get compliance data
curl http://localhost:5000/api/compliance

# Get compliance for specific app
curl http://localhost:5000/api/compliance?app=LSG

# Get summary
curl http://localhost:5000/api/compliance/summary
```

## 🐳 Docker Deployment (Optional)

### Create Dockerfile

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/compliance-dashboard.jar app.jar
EXPOSE 5000
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Build and Run

```bash
# Build the JAR
./gradlew clean build

# Build Docker image
docker build -t compliance-dashboard:1.0.0 .

# Run container
docker run -p 5000:5000 \
  -e VAULT_ADDR=https://vault.example.com \
  -e VAULT_NAMESPACE=your-namespace \
  -e VAULT_ROLE_ID=your-role-id \
  -e VAULT_SECRET_ID=your-secret-id \
  compliance-dashboard:1.0.0
```

## 🔒 Security Considerations

- ✅ Never commit `.env` files or credentials
- ✅ Use Vault for all sensitive configuration
- ✅ Rotate Vault AppRole credentials regularly
- ✅ Use HTTPS in production
- ✅ Implement authentication/authorization for the dashboard
- ✅ Review and restrict CORS origins in production
- ✅ Enable security headers
- ✅ Use secrets management for environment variables

## 📊 Monitoring & Logging

### Application Logs

```bash
# View logs in real-time
tail -f logs/application.log

# Set log level via environment
export LOG_LEVEL=DEBUG
java -jar build/libs/compliance-dashboard.jar
```

### Health Monitoring

```bash
# Application health
curl http://localhost:5000/actuator/health

# Detailed health (requires authorization)
curl http://localhost:5000/actuator/health/vault
```

### Metrics

```bash
# Application metrics
curl http://localhost:5000/actuator/metrics

# Specific metric
curl http://localhost:5000/actuator/metrics/jvm.memory.used
```

## 🛠️ Development

### Project Structure

```
src/main/java/com/compliance/dashboard/
├── ComplianceDashboardApplication.java  # Main application
├── config/
│   ├── VaultConfig.java                 # Vault configuration
│   └── WebConfig.java                   # Web & CORS config
├── controller/
│   └── ComplianceController.java        # REST endpoints
├── service/
│   ├── VaultService.java                # Vault operations
│   └── ComplianceService.java           # Compliance logic
├── client/
│   ├── IamAasClient.java                # OAuth client
│   └── OcsApiClient.java                # OCS API client
└── model/
    ├── AppConfig.java                   # App configuration
    ├── ComplianceResult.java            # Compliance result
    ├── RegionResult.java                # Region result
    └── ServerInfo.java                  # Server information
```

### Adding New Features

1. **Add new endpoint:**
   - Create method in `ComplianceController.java`
   - Add business logic in appropriate service

2. **Add new configuration:**
   - Update `application.yml`
   - Add environment variable to `.env.example`

3. **Modify frontend:**
   - Edit files in `frontend/src/`
   - Rebuild: `./gradlew buildFrontend`

## 🐛 Troubleshooting

### Build Issues

**Problem:** Gradle build fails
```bash
# Solution: Clean and rebuild
./gradlew clean build --refresh-dependencies
```

**Problem:** Frontend build fails
```bash
# Solution: Clean frontend and rebuild
rm -rf frontend/node_modules frontend/dist
./gradlew buildFrontend
```

### Runtime Issues

**Problem:** "Failed to authenticate with Vault"
```bash
# Solution: Verify Vault credentials
echo $VAULT_ADDR
echo $VAULT_ROLE_ID
# Test Vault connection manually
```

**Problem:** "Port 5000 already in use"
```bash
# Solution: Use different port
export PORT=8080
java -jar build/libs/compliance-dashboard.jar
```

**Problem:** Frontend not loading
```bash
# Solution: Verify static resources were copied
ls -la build/resources/main/static/
# Rebuild if empty
./gradlew clean build
```

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Vault Documentation](https://spring.io/projects/spring-vault)
- [Gradle Documentation](https://docs.gradle.org/)
- [React Documentation](https://react.dev/)

## 📝 License

[Your License Here]

## 👥 Support

For issues and questions, please contact [Your Contact Information]

---

**Built with ❤️ using Spring Boot + React**
