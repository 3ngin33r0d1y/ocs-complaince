import { RefreshCw, Settings } from 'lucide-react'

function Header({ 
  onRefresh, 
  autoRefresh, 
  onAutoRefreshToggle, 
  refreshInterval, 
  onIntervalChange,
  selectedApp,
  availableApps,
  onAppChange,
  loading 
}) {
  const intervalOptions = [
    { value: 60000, label: '1 minute' },
    { value: 300000, label: '5 minutes' },
    { value: 600000, label: '10 minutes' },
    { value: 1800000, label: '30 minutes' },
  ]

  return (
    <header className="bg-white dark:bg-gray-800 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
              OCS Compliance Dashboard
            </h1>
            <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
              Monitor server compliance across regions
            </p>
          </div>
          
          <div className="flex flex-wrap items-center gap-3">
            {/* App Selector */}
            <select
              value={selectedApp}
              onChange={(e) => onAppChange(e.target.value)}
              className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              disabled={loading}
            >
              <option value="all">All Apps</option>
              {availableApps.map(app => (
                <option key={app} value={app}>{app}</option>
              ))}
            </select>

            {/* Auto-refresh Toggle */}
            <label className="flex items-center gap-2 text-sm text-gray-700 dark:text-gray-300">
              <input
                type="checkbox"
                checked={autoRefresh}
                onChange={onAutoRefreshToggle}
                className="w-4 h-4 text-primary-600 bg-gray-100 border-gray-300 rounded focus:ring-primary-500 dark:focus:ring-primary-600 dark:ring-offset-gray-800 focus:ring-2 dark:bg-gray-700 dark:border-gray-600"
              />
              Auto-refresh
            </label>

            {/* Interval Selector */}
            {autoRefresh && (
              <select
                value={refreshInterval}
                onChange={(e) => onIntervalChange(Number(e.target.value))}
                className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              >
                {intervalOptions.map(option => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            )}

            {/* Manual Refresh Button */}
            <button
              onClick={onRefresh}
              disabled={loading}
              className="inline-flex items-center gap-2 px-4 py-2 bg-primary-600 hover:bg-primary-700 disabled:bg-gray-400 text-white font-medium rounded-lg transition-colors duration-200"
            >
              <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
              Refresh
            </button>
          </div>
        </div>
      </div>
    </header>
  )
}

export default Header
