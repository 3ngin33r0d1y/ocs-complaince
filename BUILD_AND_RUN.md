# 🚀 Build and Run Guide

Complete guide to build and run the OCS Compliance Dashboard (Spring Boot + React).

## 📋 Prerequisites

### 1. Check Java Installation

```bash
java -version
```

**Required:** Java 17 or higher

If not installed:

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

### 2. Verify Java is in PATH

```bash
which java
echo $JAVA_HOME
```

If JAVA_HOME is not set:

**macOS:**
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

**Linux:**
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
```

Add to `~/.bashrc` or `~/.zshrc` to make permanent.

## 🔧 Setup

### 1. Configure Environment

```bash
# Copy example environment file
cp .env.example .env

# Edit with your credentials
nano .env
```

**Required variables:**
```bash
VAULT_ADDR=https://your-vault-server.com
VAULT_NAMESPACE=your-namespace
VAULT_ROLE_ID=your-role-id
VAULT_SECRET_ID=your-secret-id
VAULT_CONFIG_PATH=compliance/config
PORT=5000
LOG_LEVEL=INFO
```

### 2. Load Environment Variables

```bash
# Load from .env file
export $(cat .env | xargs)

# Verify
echo $VAULT_ADDR
echo $PORT
```

## 🏗️ Build

### Option 1: Full Build (Recommended)

Builds both backend and frontend in one command:

```bash
./gradlew clean build
```

**What this does:**
1. ✅ Downloads Gradle (if needed)
2. ✅ Downloads Node.js and npm automatically
3. ✅ Installs frontend dependencies (`npm install`)
4. ✅ Builds React frontend (`npm run build`)
5. ✅ Copies frontend to Spring Boot static resources
6. ✅ Compiles Java backend
7. ✅ Runs tests
8. ✅ Packages as single JAR: `build/libs/compliance-dashboard.jar`

**Build time:** 2-5 minutes (first time), 30-60 seconds (subsequent builds)

### Option 2: Build Without Tests

```bash
./gradlew clean build -x test
```

### Option 3: Backend Only

```bash
./gradlew clean bootJar -x buildFrontend
```

### Option 4: Frontend Only

```bash
./gradlew buildFrontend
```

## 🏃 Run

### Option 1: Run the JAR (Production Mode)

```bash
# Make sure environment variables are loaded
export $(cat .env | xargs)

# Run the application
java -jar build/libs/compliance-dashboard.jar
```

### Option 2: Run with Gradle (Development Mode)

```bash
./gradlew bootRun
```

This provides:
- Auto-reload on code changes (with Spring DevTools)
- Debug logging
- Development profiles

### Option 3: Run with Custom Port

```bash
export PORT=8080
java -jar build/libs/compliance-dashboard.jar
```

### Option 4: Run with Increased Memory

```bash
java -Xmx2g -jar build/libs/compliance-dashboard.jar
```

## ✅ Verify

### 1. Check Application Started

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

### 2. Test Health Endpoint

```bash
curl http://localhost:5000/api/health
```

Expected response:
```json
{
  "status": "healthy",
  "vault_connected": true,
  "message": "API is running and Vault is accessible"
}
```

### 3. Test Apps Endpoint

```bash
curl http://localhost:5000/api/apps
```

### 4. Access Frontend

Open browser: http://localhost:5000

You should see the OCS Compliance Dashboard! 🎉

## 🐛 Troubleshooting

### Build Issues

#### Problem: "JAVA_HOME not set"

```bash
# macOS
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Linux
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk

# Verify
echo $JAVA_HOME
```

#### Problem: "Permission denied: ./gradlew"

```bash
chmod +x gradlew
```

#### Problem: Frontend build fails

```bash
# Clean and rebuild
rm -rf frontend/node_modules frontend/dist
./gradlew clean build
```

#### Problem: "Could not resolve dependencies"

```bash
# Refresh dependencies
./gradlew clean build --refresh-dependencies
```

### Runtime Issues

#### Problem: "Failed to authenticate with Vault"

```bash
# Verify environment variables
echo $VAULT_ADDR
echo $VAULT_ROLE_ID
echo $VAULT_SECRET_ID

# Test Vault manually (if vault CLI is installed)
vault login -method=approle \
  role_id=$VAULT_ROLE_ID \
  secret_id=$VAULT_SECRET_ID
```

#### Problem: "Port 5000 already in use"

```bash
# Find process using port
lsof -ti:5000

# Kill process
lsof -ti:5000 | xargs kill -9

# Or use different port
export PORT=8080
java -jar build/libs/compliance-dashboard.jar
```

#### Problem: "OutOfMemoryError"

```bash
# Increase heap size
java -Xmx2g -Xms512m -jar build/libs/compliance-dashboard.jar
```

#### Problem: Frontend shows blank page

```bash
# Verify static resources exist
ls -la build/resources/main/static/

# If empty, rebuild
./gradlew clean build

# Check browser console for errors (F12)
```

### Vault Issues

#### Problem: "No data found at Vault path"

```bash
# Verify Vault path
echo $VAULT_CONFIG_PATH

# Check if data exists in Vault
vault kv get secret/$VAULT_CONFIG_PATH
```

## 📊 Monitoring

### View Logs

```bash
# Real-time logs
tail -f logs/application.log

# Last 100 lines
tail -n 100 logs/application.log

# Search for errors
grep "ERROR" logs/application.log
```

### Health Checks

```bash
# Application health
curl http://localhost:5000/actuator/health

# Vault health
curl http://localhost:5000/actuator/health/vault

# All actuator endpoints
curl http://localhost:5000/actuator
```

### Metrics

```bash
# All metrics
curl http://localhost:5000/actuator/metrics

# JVM memory
curl http://localhost:5000/actuator/metrics/jvm.memory.used

# HTTP requests
curl http://localhost:5000/actuator/metrics/http.server.requests
```

## 🔄 Development Workflow

### 1. Make Backend Changes

```bash
# Edit Java files in src/main/java/

# Rebuild and run
./gradlew clean bootRun
```

### 2. Make Frontend Changes

```bash
# Option A: Rebuild frontend only
./gradlew buildFrontend

# Option B: Use frontend dev server (hot reload)
cd frontend
npm run dev
# Access at http://localhost:3000
```

### 3. Full Rebuild

```bash
./gradlew clean build
```

## 🚀 Deployment

### 1. Build Production JAR

```bash
./gradlew clean build -x test
```

### 2. Copy to Server

```bash
scp build/libs/compliance-dashboard.jar user@server:/opt/compliance/
scp .env user@server:/opt/compliance/
```

### 3. Run on Server

```bash
ssh user@server
cd /opt/compliance
export $(cat .env | xargs)
nohup java -jar compliance-dashboard.jar > app.log 2>&1 &
```

### 4. Create Systemd Service (Linux)

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

Enable and start:

```bash
sudo systemctl daemon-reload
sudo systemctl enable compliance-dashboard
sudo systemctl start compliance-dashboard
sudo systemctl status compliance-dashboard
```

## 📦 Docker Deployment

### 1. Create Dockerfile

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/compliance-dashboard.jar app.jar
EXPOSE 5000
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Build and Run

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

## 🎯 Quick Reference

```bash
# Build
./gradlew clean build              # Full build
./gradlew build -x test            # Skip tests
./gradlew bootJar                  # Backend only

# Run
./gradlew bootRun                  # Development
java -jar build/libs/*.jar         # Production

# Test
./gradlew test                     # Run tests
curl http://localhost:5000/api/health  # Test API

# Clean
./gradlew clean                    # Clean build
rm -rf build frontend/dist         # Deep clean

# Logs
tail -f logs/application.log       # View logs
```

## ✨ Success!

If everything is working:
- ✅ Build completes without errors
- ✅ Application starts successfully
- ✅ Health check returns "healthy"
- ✅ Frontend loads at http://localhost:5000
- ✅ API endpoints respond correctly

**You're ready to go! 🚀**

For more details, see:
- README-NEW.md - Complete documentation
- SETUP_GUIDE-NEW.md - Setup instructions
