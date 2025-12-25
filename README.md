# OCS Compliance Dashboard

A comprehensive web application for monitoring server compliance across multiple cloud regions. The dashboard validates whether servers are running images built in the current ISO week, providing real-time compliance tracking with interactive visualizations.

## Features

- **Spring Boot API** with Vault (AppRole) configuration
- **Compliance engine** that checks current ISO week images across regions
- **React dashboard** with charts, filters, and sortable tables
- **Bundled build**: frontend is baked into the backend JAR

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

- Java 17+
- Access to HashiCorp Vault
- Valid Vault AppRole credentials
- Network access to OCS APIs
- Node.js 18+ (only needed to build the JAR)

## Installation

```bash
cd compliance-dashboard
cp .env.example .env
# Edit .env with your Vault credentials
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

## Build and Run (Single JAR)

```bash
./gradlew bootJar
java -jar build/libs/compliance-dashboard.jar
```

Open `http://localhost:8080` to access the dashboard and API.

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

1. **Access the Dashboard**: Open `http://localhost:8080` in your browser
2. **Select Application**: Use the dropdown in the header to select a specific app or view all apps
3. **View Compliance**: Dashboard, charts, and server table update automatically
4. **Filter & Search**: Filter by status/region and search for servers or images
5. **Auto-refresh**: Toggle refresh and pick the interval

## Compliance Logic

The system classifies servers based on their image build week:

- **Compliant (Good)**: Image built in the current ISO week
- **Non-Compliant (Bad)**:
  - Images from previous weeks/years
  - Images with unparsable week information
  - Images with future dates (treated as non-compliant)

Image names must follow the pattern: `*_YYYY_wWW_*` (e.g., `ocs_dev_RHEL_9_2025_w41_legacy`)

## Troubleshooting

### Vault Connection Issues
- Verify `VAULT_ADDR`, `VAULT_NAMESPACE` are correct
- Check `VAULT_ROLE_ID` and `VAULT_SECRET_ID` are valid
- Ensure network connectivity to Vault server

### API Connection Issues
- Verify the JAR is running on port 8080
- Check CORS configuration if accessing from different origin

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

DevOps Team Casablanca, AFMO Lions.
