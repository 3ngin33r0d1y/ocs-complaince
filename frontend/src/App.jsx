import { useState, useEffect } from 'react'
import axios from 'axios'
import Dashboard from './components/Dashboard'
import ComplianceTable from './components/ComplianceTable'
import ComplianceCharts from './components/ComplianceCharts'
import Header from './components/Header'
import LoadingSpinner from './components/LoadingSpinner'
import ErrorMessage from './components/ErrorMessage'
import LoginPage from './components/LoginPage'

const API_BASE_URL = import.meta.env.VITE_API_URL || ''

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(() => {
    return window.localStorage.getItem('cd-auth') === 'true'
  })
  const [userEmail, setUserEmail] = useState(() => {
    return window.localStorage.getItem('cd-user') || ''
  })
  const [complianceData, setComplianceData] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [autoRefresh, setAutoRefresh] = useState(false)
  const [refreshInterval, setRefreshInterval] = useState(300000) // 5 minutes default
  const [selectedApp, setSelectedApp] = useState('')
  const [availableApps, setAvailableApps] = useState([])

  // Fetch available apps
  useEffect(() => {
    if (!isAuthenticated) {
      return
    }
    fetchApps()
  }, [isAuthenticated])

  // Auto-refresh logic
  useEffect(() => {
    if (isAuthenticated && autoRefresh) {
      const interval = setInterval(() => {
        fetchComplianceData()
      }, refreshInterval)
      return () => clearInterval(interval)
    }
  }, [autoRefresh, refreshInterval, selectedApp, isAuthenticated])

  const fetchApps = async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/api/apps`)
      const apps = (response.data.apps || []).slice().sort((a, b) => a.localeCompare(b))
      setAvailableApps(apps)
      if (!selectedApp && apps.length > 0) {
        setSelectedApp(apps[0])
      }
    } catch (err) {
      console.error('Error fetching apps:', err)
    }
  }

  const fetchComplianceData = async () => {
    if (!selectedApp) {
      return
    }
    setLoading(true)
    setError(null)
    
    try {
      const url = selectedApp === 'all' 
        ? `${API_BASE_URL}/api/compliance`
        : `${API_BASE_URL}/api/compliance?app=${selectedApp}`
      
      const response = await axios.get(url)
      setComplianceData(response.data)
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to fetch compliance data')
      console.error('Error fetching compliance data:', err)
    } finally {
      setLoading(false)
    }
  }

  // Initial fetch
  useEffect(() => {
    if (!isAuthenticated) {
      return
    }
    fetchComplianceData()
  }, [selectedApp, isAuthenticated])

  const handleRefresh = () => {
    fetchComplianceData()
  }

  const handleAppChange = (app) => {
    setSelectedApp(app)
  }

  const handleAutoRefreshToggle = () => {
    setAutoRefresh(!autoRefresh)
  }

  const handleIntervalChange = (interval) => {
    setRefreshInterval(interval)
  }

  const handleLogin = (email) => {
    window.localStorage.setItem('cd-auth', 'true')
    window.localStorage.setItem('cd-user', email)
    setUserEmail(email)
    setIsAuthenticated(true)
  }

  const handleSignOut = () => {
    window.localStorage.removeItem('cd-auth')
    window.localStorage.removeItem('cd-user')
    setIsAuthenticated(false)
    setUserEmail('')
    setComplianceData(null)
    setError(null)
    setSelectedApp('')
    setAvailableApps([])
    setAutoRefresh(false)
  }

  if (!isAuthenticated) {
    return <LoginPage onLogin={handleLogin} />
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <Header 
        onRefresh={handleRefresh}
        autoRefresh={autoRefresh}
        onAutoRefreshToggle={handleAutoRefreshToggle}
        refreshInterval={refreshInterval}
        onIntervalChange={handleIntervalChange}
        selectedApp={selectedApp}
        availableApps={availableApps}
        onAppChange={handleAppChange}
        loading={loading}
        userEmail={userEmail}
        onSignOut={handleSignOut}
      />
      
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {error && (
          <ErrorMessage message={error} onDismiss={() => setError(null)} />
        )}
        
        {loading && !complianceData && (
          <LoadingSpinner />
        )}
        
        {complianceData && (
          <div className="space-y-6">
            <Dashboard data={complianceData} />
            <ComplianceCharts data={complianceData} />
            <ComplianceTable data={complianceData} />
          </div>
        )}
        
        {!loading && !complianceData && !error && (
          <div className="text-center py-12">
            <p className="text-gray-500 dark:text-gray-400">
              Click refresh to load compliance data
            </p>
          </div>
        )}
      </main>
    </div>
  )
}

export default App
