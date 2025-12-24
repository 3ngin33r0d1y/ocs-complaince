# OCS Compliance Dashboard

A comprehensive web application for monitoring server compliance across multiple cloud regions. The dashboard validates whether servers are running images built in the current ISO week, providing real-time compliance tracking with interactive visualizations.

## Features

### Backend
- **Vault Integration**: Secure configuration management using HashiCorp Vault with AppRole authentication
- **Multi-Region Support**: Monitors servers across multiple regions (Paris, North)
- **RESTful API**: Spring Boot API with CORS support
- **Compliance Checking**: Validates server images against current ISO week
- **Retry Logic**: Robust error handling with configurable retries and timeouts

### Frontend
- **React SPA**: Modern single-page application built with React and Vite
- **Real-time Dashboard**: KPI cards showing total servers, compliance rates, and statistics
- **Interactive Charts**: Pie charts and bar charts for visual compliance analysis
- **Advanced Filtering**: Search, filter by status/region/app, and sort capabilities
- **Auto-refresh**: Configurable automatic data refresh (1-30 minutes)
- **Responsive Design**: Mobile-friendly UI with Tailwind CSS
- **Dark Mode**: Full dark mode support

## Architecture

```
compliance-dashboard/
├── src/
│   ├── main/java/                # Spring Boot backend
│   └── main/resources/           # Application configuration
├── frontend/
│   ├── src/
│   │   ├── components/          # React components
│   │   │   ├── Dashboard.jsx    # KPI cards
│   │   │   ├── ComplianceCharts.jsx  # Charts
│   │   │   ├── ComplianceTable.jsx   # Data table
│   │   │   ├── Header.jsx       # App header with controls
│   │   │   ├── LoadingSpinner.jsx
│   │   │   └── ErrorMessage.jsx
│   │   ├── App.jsx              # Main app component
│   │   ├── main.jsx             # Entry point
│   │   └── index.css            # Global styles
│   ├── package.json
│   ├── vite.config.js
│   └── tailwind.config.js
└── README.md
```

## Prerequisites

### Backend
- Java 17+
- Access to HashiCorp Vault
- Valid Vault AppRole credentials
- Network access to OCS APIs

### Frontend
- Node.js 18+
- npm or yarn

## Installation

### 1. Backend Setup

```bash
cd compliance-dashboard

# Configure environment variables
cp .env.example .env
# Edit .env with your Vault credentials
```

### 2. Frontend Setup

```bash
cd compliance-dashboard/frontend

# Install dependencies
npm install

# Configure environment variables
cp .env.example .env
# Edit .env if needed (default: http://localhost:5000)
```

## Configuration

### Backend Environment Variables

Create a `.env` file in the project root:

```bash
# Vault Configuration
VAULT_ADDR=https://vault.example.com
VAULT_NAMESPACE=your-namespace
VAULT_ROLE_ID=your-role-id
VAULT_SECRET_ID=your-secret-id
VAULT_CONFIG_PATH=compliance/config

# Server Configuration
PORT=5000

```

### Vault Configuration Structure

Store your application configuration in Vault at the specified path (e.g., `secret/data/compliance/config`):

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

### Frontend Environment Variables

Create a `.env` file in the `frontend/` directory:

```bash
# API Configuration
VITE_API_URL=http://localhost:5000
```

## Running the Application

### Start Backend Server

```bash
cd compliance-dashboard
./gradlew bootRun
```

The API will be available at `http://localhost:5000`. The frontend build is bundled into the Spring Boot app when you build the JAR.


### Start Frontend Development Server

```bash
cd compliance-dashboard/frontend
npm run dev
```

The frontend will be available at `http://localhost:3000`

## API Endpoints

### Health Check
```
GET /api/health
```
Returns API health status and Vault connectivity.

### Get Available Apps
```
GET /api/apps
```
Returns list of applications configured in Vault.

### Get Compliance Data
```
GET /api/compliance?app=<app_name>&debug=<true|false>
```
- `app` (optional): Specific app to check. If omitted, checks all apps.
- `debug` (optional): Enable debug logging.

Returns compliance data for specified app(s) across all regions.

### Get Compliance Summary
```
GET /api/compliance/summary?debug=<true|false>
```
Returns aggregated compliance statistics across all apps and regions.

## Usage

1. **Access the Dashboard**: Open `http://localhost:3000` in your browser

2. **Select Application**: Use the dropdown in the header to select a specific app or view all apps

3. **View Compliance**: 
   - Dashboard shows overall statistics
   - Charts provide visual analysis
   - Table lists all servers with detailed information

4. **Filter & Search**:
   - Use the search box to find specific servers or images
   - Filter by status (compliant/non-compliant)
   - Filter by region
   - Sort columns by clicking headers

5. **Auto-refresh**:
   - Toggle auto-refresh in the header
   - Select refresh interval (1-30 minutes)
   - Manual refresh available anytime

## Compliance Logic

The system classifies servers based on their image build week:

- **Compliant (Good)**: Image built in the current ISO week
- **Non-Compliant (Bad)**:
  - Images from previous weeks/years
  - Images with unparsable week information
  - Images with future dates (treated as non-compliant)

Image names must follow the pattern: `*_YYYY_wWW_*` (e.g., `ocs_dev_RHEL_9_2025_w41_legacy`)

## Development

### Backend Development

```bash
# Run the Spring Boot app
./gradlew bootRun

# Run tests (if implemented)
./gradlew test
```

### Frontend Development

```bash
# Development server with hot reload
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Lint code
npm run lint
```

## Production Deployment

### Backend

```bash
./gradlew bootJar
java -jar build/libs/compliance-dashboard.jar
```

### Frontend

```bash
# Build for production
npm run build

# Serve the dist/ folder with a web server (nginx, Apache, etc.)
```

## Troubleshooting

### Vault Connection Issues
- Verify `VAULT_ADDR`, `VAULT_NAMESPACE` are correct
- Check `VAULT_ROLE_ID` and `VAULT_SECRET_ID` are valid
- Ensure network connectivity to Vault server

### API Connection Issues
- Verify backend is running on correct port
- Check CORS configuration if accessing from different origin
- Verify `VITE_API_URL` in frontend `.env`

### No Data Displayed
- Check browser console for errors
- Verify Vault contains valid configuration
- Check backend logs for API errors
- Ensure OCS API endpoints are accessible

## Security Considerations

- Never commit `.env` files or credentials to version control
- Use Vault for all sensitive configuration
- Rotate Vault AppRole credentials regularly
- Use HTTPS in production
- Implement proper authentication/authorization for the dashboard
- Review and restrict CORS origins in production

## License

[Your License Here]

## Support

For issues and questions, please contact [Your Contact Information]
