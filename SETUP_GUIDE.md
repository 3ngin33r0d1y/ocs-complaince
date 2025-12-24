# Quick Setup Guide

This guide will help you get the OCS Compliance Dashboard up and running quickly.

## Prerequisites Checklist

- [ ] Java 17+ installed
- [ ] Node.js 18+ installed
- [ ] Access to HashiCorp Vault
- [ ] Vault AppRole credentials (role_id, secret_id)
- [ ] Network access to OCS APIs

## Step-by-Step Setup

### 1. Clone/Download the Project

```bash
cd ~/Desktop
# If you have the project files, navigate to the compliance-dashboard directory
cd compliance-dashboard
```

### 2. Backend Setup (5 minutes)

```bash
# From the project root
cd ~/Desktop/compliance-dashboard

# Configure environment variables
cp .env.example .env
nano .env  # or use your preferred editor
```

**Edit `.env` file with your credentials:**

```bash
VAULT_ADDR=https://your-vault-server.com
VAULT_NAMESPACE=your-namespace
VAULT_ROLE_ID=your-role-id-here
VAULT_SECRET_ID=your-secret-id-here
VAULT_CONFIG_PATH=compliance/config
PORT=5000
```

**Save and test the backend:**

```bash
./gradlew bootRun
```

You should see:
```
... Started ComplianceDashboardApplication ...
```

Test the health endpoint:
```bash
curl http://localhost:5000/api/health
```


### 3. Frontend Setup (5 minutes)

Open a **new terminal window** and:

```bash
# Navigate to frontend directory
cd ~/Desktop/compliance-dashboard/frontend

# Install dependencies (this may take a few minutes)
npm install

# Configure environment (optional - defaults work for local development)
cp .env.example .env

# Start development server
npm run dev
```

You should see:
```
  VITE v5.x.x  ready in xxx ms

  ➜  Local:   http://localhost:3000/
```

### 4. Access the Dashboard

Open your browser and navigate to:
```
http://localhost:3000
```

You should see the OCS Compliance Dashboard!

## Vault Configuration

Ensure your Vault contains the application configuration at the specified path.

### Example Vault Setup

```bash
# Login to Vault
vault login -method=approle role_id="your-role-id" secret_id="your-secret-id"

# Write configuration
vault kv put secret/compliance/config \
  LSG='{"account_id":"xxx","client_id":"xxx","client_secret":"xxx","iamaas_url":"https://iamaas.example.com/oauth/token","sgcp_iamaas_scopes":"ocs:read ccs:read"}'

# Verify
vault kv get secret/compliance/config
```

Or use the Vault UI to create the configuration as JSON:

```json
{
  "LSG": {
    "account_id": "your-account-id",
    "client_id": "your-client-id",
    "client_secret": "your-client-secret",
    "iamaas_url": "https://iamaas.example.com/oauth/token",
    "sgcp_iamaas_scopes": "ocs:read ccs:read"
  }
}
```

## Troubleshooting

### Backend Issues

**Problem: "Missing required environment variables"**
```bash
# Solution: Check your .env file
cat .env
# Ensure all required variables are set
```

**Problem: "Failed to authenticate with Vault"**
```bash
# Solution: Verify Vault credentials
# Test Vault connection manually:
vault login -method=approle role_id="your-role-id" secret_id="your-secret-id"
```

### Frontend Issues

**Problem: "Cannot find module"**
```bash
# Solution: Reinstall dependencies
rm -rf node_modules package-lock.json
npm install
```

**Problem: "Failed to fetch compliance data"**
```bash
# Solution: Check backend is running
curl http://localhost:5000/api/health

# Verify VITE_API_URL in frontend/.env
```

**Problem: Port 3000 already in use**
```bash
# Solution: Use a different port
npm run dev -- --port 3001
```

## Testing the Application

### 1. Test Backend API

```bash
# Health check
curl http://localhost:5000/api/health

# Get available apps
curl http://localhost:5000/api/apps

# Get compliance data (this will take a moment)
curl http://localhost:5000/api/compliance
```

### 2. Test Frontend

1. Open http://localhost:3000
2. Click "Refresh" button
3. You should see:
   - Dashboard with statistics
   - Charts showing compliance data
   - Table with server details

### 3. Test Features

- [ ] Select different apps from dropdown
- [ ] Toggle auto-refresh
- [ ] Change refresh interval
- [ ] Search for servers
- [ ] Filter by status/region
- [ ] Sort table columns
- [ ] Verify charts update correctly

## Production Deployment

### Backend (Production)

```bash
./gradlew bootJar
java -jar build/libs/compliance-dashboard.jar
```

### Frontend (Production)

```bash
# Build for production
npm run build

# The dist/ folder contains production files
# Serve with nginx, Apache, or any static file server
```

### Example Nginx Configuration

```nginx
server {
    listen 80;
    server_name compliance-dashboard.example.com;

    # Frontend
    location / {
        root /path/to/compliance-dashboard/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # Backend API
    location /api {
        proxy_pass http://localhost:5000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## Next Steps

1. **Customize**: Modify colors, branding in `frontend/src/index.css` and `tailwind.config.js`
2. **Add Authentication**: Implement user authentication for the dashboard
3. **Monitoring**: Add logging and monitoring for production
4. **Alerts**: Set up alerts for low compliance rates
5. **Backup**: Implement backup strategy for Vault data

## Support

If you encounter issues:

1. Check the logs:
   - Backend: Terminal where `./gradlew bootRun` is running
   - Frontend: Browser console (F12)

2. Verify environment variables are correct

3. Ensure network connectivity to Vault and OCS APIs

4. Review the main README.md for detailed documentation

## Quick Reference

### Start Development

```bash
# Terminal 1 - Backend
cd compliance-dashboard
./gradlew bootRun

# Terminal 2 - Frontend
cd compliance-dashboard/frontend
npm run dev
```

### Stop Services

```bash
# Press Ctrl+C in each terminal
```

### Update Dependencies

```bash
# Backend
./gradlew build

# Frontend
npm update
```

---

**Congratulations!** 🎉 Your OCS Compliance Dashboard is now running!
