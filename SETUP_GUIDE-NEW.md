# Quick Setup Guide - Spring Boot + React

Get the OCS Compliance Dashboard running in minutes!

## ⚡ Quick Start

### Prerequisites Checklist

- [ ] Java 17+ installed (`java -version`)
- [ ] Gradle installed (or use `./gradlew`)
- [ ] Access to HashiCorp Vault
- [ ] Vault AppRole credentials
- [ ] Network access to OCS APIs

### 1. Verify Java Installation

```bash
java -version
# Should show: java version "17" or higher
```

If Java is not installed:

**macOS:**
```bash
brew install openjdk@17
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

**Linux (RHEL/CentOS):**
```bash
sudo yum install java-17-openjdk-devel
```

### 2. Clone and Configure

```bash
# Navigate to your workspace
cd ~/Desktop

# Clone repository (or navigate to existing project)
cd compliance-dashboard

# Create environment file
cp .env.example .env

# Edit with your credentials
nano .env  # or use your preferred editor
```

**Required Environment Variables:**

```bash
VAULT_ADDR=https://your-vault-server.com
VAULT_NAMESPACE=your-namespace
VAULT_ROLE_ID=your-role-id-here
VAULT_SECRET_ID=your-secret-id-here
VAULT_CONFIG_PATH=compliance/config
PORT=5000
LOG_LEVEL=INFO
```

### 3. Build the Application

```bash
# Build everything (backend + frontend) in one command
./gradlew clean build

# This will:
# ✓ Download Node.js and npm automatically
# ✓ Install frontend dependencies
# ✓ Build React frontend
# ✓ Compile Java backend
# ✓ Package as single JAR file
```

**Build time:** ~2-5 minutes (first time)

### 4. Run the Application

```bash
# Load environment variables
export $(cat .env | xargs)

# Run the JAR
java -jar build/libs/compliance-dashboard.jar
```

You should see:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

...
Started ComplianceDashboardApplication in X.XXX seconds
```

### 5. Access the Dashboard

Open your browser and navigate to:

```
http://localhost:5000
```

You should see the OCS Compliance Dashboard! 🎉

## 🧪 Test the API

```bash
# Health check
curl http://localhost:5000/api/health

# Get available apps
curl http://localhost:5000/api/apps

# Get compliance data
curl http://localhost:5000/api/compliance
```

## 🔄 Development Workflow

### Run in Development Mode

```bash
# Terminal 1: Run backend with auto-reload
./gradlew bootRun

# Terminal 2: Run frontend dev server (optional)
cd frontend
npm run dev
```

### Make Changes

1. **Backend changes:**
   - Edit Java files in `src/main/java/`
   - Restart: `./gradlew bootRun`

2. **Frontend changes:**
   - Edit React files in `frontend/src/`
   - Rebuild: `./gradlew buildFrontend`
   - Or use frontend dev server for hot reload

### Rebuild Everything

```bash
./gradlew clean build
```

## 🐳 Docker Deployment

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
# Build JAR
./gradlew clean build

# Build Docker image
docker build -t compliance-dashboard:1.0.0 .

# Run container
docker run -p 5000:5000 \
  --env-file .env \
  compliance-dashboard:1.0.0
```

## 🚀 Production Deployment

### 1. Build Production JAR

```bash
./gradlew clean build -x test
```

### 2. Copy to Server

```bash
scp build/libs/compliance-dashboard.jar user@server:/opt/compliance/
scp .env user@server:/opt/compliance/
```

### 3. Create Systemd Service

Create `/etc/systemd/system/compliance-dashboard.service`:

```ini
[Unit]
Description=OCS Compliance Dashboard
After=network.target

[Service]
Type=simple
User=compliance
WorkingDirectory=/opt/compliance
EnvironmentFile=/opt/compliance/.env
ExecStart=/usr/bin/java -jar /opt/compliance/compliance-dashboard.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

### 4. Start Service

```bash
sudo systemctl daemon-reload
sudo systemctl enable compliance-dashboard
sudo systemctl start compliance-dashboard
sudo systemctl status compliance-dashboard
```

## 🔧 Troubleshooting

### Build Fails

**Problem:** `JAVA_HOME not set`
```bash
# Solution: Set JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 17)  # macOS
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk     # Linux
```

**Problem:** `Permission denied: ./gradlew`
```bash
# Solution: Make gradlew executable
chmod +x gradlew
```

**Problem:** Frontend build fails
```bash
# Solution: Clean and rebuild
rm -rf frontend/node_modules frontend/dist
./gradlew clean build
```

### Runtime Issues

**Problem:** `Failed to authenticate with Vault`
```bash
# Solution: Verify credentials
echo $VAULT_ADDR
echo $VAULT_ROLE_ID

# Test Vault manually
vault login -method=approle role_id=$VAULT_ROLE_ID secret_id=$VAULT_SECRET_ID
```

**Problem:** `Port 5000 already in use`
```bash
# Solution: Use different port
export PORT=8080
java -jar build/libs/compliance-dashboard.jar

# Or kill process using port 5000
lsof -ti:5000 | xargs kill -9
```

**Problem:** `OutOfMemoryError`
```bash
# Solution: Increase heap size
java -Xmx2g -jar build/libs/compliance-dashboard.jar
```

### Frontend Not Loading

**Problem:** Blank page or 404 errors
```bash
# Solution: Verify static resources
ls -la build/resources/main/static/

# If empty, rebuild
./gradlew clean build
```

## 📊 Monitoring

### View Logs

```bash
# Real-time logs
tail -f logs/application.log

# Last 100 lines
tail -n 100 logs/application.log

# Search logs
grep "ERROR" logs/application.log
```

### Health Checks

```bash
# Application health
curl http://localhost:5000/actuator/health

# Vault health
curl http://localhost:5000/actuator/health/vault

# Metrics
curl http://localhost:5000/actuator/metrics
```

## 🔐 Security Checklist

- [ ] Change default port in production
- [ ] Use HTTPS (reverse proxy with nginx/Apache)
- [ ] Restrict CORS origins
- [ ] Enable authentication
- [ ] Rotate Vault credentials regularly
- [ ] Use secrets management (not .env files)
- [ ] Enable security headers
- [ ] Set up monitoring and alerting

## 📚 Next Steps

1. **Customize:** Modify branding in `frontend/src/`
2. **Add Features:** Extend API in `ComplianceController.java`
3. **Monitor:** Set up logging and metrics
4. **Scale:** Deploy multiple instances with load balancer
5. **Secure:** Implement authentication and authorization

## 🆘 Getting Help

- Check logs: `tail -f logs/application.log`
- Review README-NEW.md for detailed documentation
- Test API endpoints with curl
- Verify Vault connectivity

## 🎯 Common Commands Reference

```bash
# Build
./gradlew clean build              # Full build
./gradlew build -x test            # Skip tests
./gradlew bootJar                  # Backend only

# Run
./gradlew bootRun                  # Development mode
java -jar build/libs/*.jar         # Production mode

# Test
./gradlew test                     # Run tests
curl http://localhost:5000/api/health  # Test API

# Clean
./gradlew clean                    # Clean build
rm -rf build frontend/dist         # Deep clean
```

---

**You're all set! 🚀**

Access your dashboard at: http://localhost:5000
