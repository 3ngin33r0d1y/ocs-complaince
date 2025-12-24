import { useState, useMemo } from 'react'
import { Search, ChevronDown, ChevronUp } from 'lucide-react'

function ComplianceTable({ data }) {
  const [searchTerm, setSearchTerm] = useState('')
  const [filterStatus, setFilterStatus] = useState('all') // all, compliant, non-compliant
  const [filterRegion, setFilterRegion] = useState('all')
  const [filterApp, setFilterApp] = useState('all')
  const [sortConfig, setSortConfig] = useState({ key: null, direction: 'asc' })

  // Flatten data into table rows
  const tableData = useMemo(() => {
    const rows = []

    if (data.apps) {
      // Multiple apps
      Object.entries(data.apps).forEach(([appName, appData]) => {
        Object.entries(appData.regions || {}).forEach(([region, regionData]) => {
          if (!regionData.error) {
            // Add compliant servers
            regionData.good_servers?.forEach(server => {
              rows.push({
                app: appName,
                region,
                serverName: server.name,
                imageName: server.image_name,
                imageId: server.image_id,
                status: 'compliant',
                imageYear: server.image_year,
                imageWeek: server.image_week,
                reason: 'Current week'
              })
            })

            // Add non-compliant servers
            regionData.bad_servers?.forEach(server => {
              rows.push({
                app: appName,
                region,
                serverName: server.name,
                imageName: server.image_name,
                imageId: server.image_id,
                status: 'non-compliant',
                imageYear: server.image_year,
                imageWeek: server.image_week,
                reason: server.reason || 'Unknown'
              })
            })
          }
        })
      })
    } else if (data.regions) {
      // Single app
      const appName = data.app_name || 'Unknown'
      Object.entries(data.regions).forEach(([region, regionData]) => {
        if (!regionData.error) {
          regionData.good_servers?.forEach(server => {
            rows.push({
              app: appName,
              region,
              serverName: server.name,
              imageName: server.image_name,
              imageId: server.image_id,
              status: 'compliant',
              imageYear: server.image_year,
              imageWeek: server.image_week,
              reason: 'Current week'
            })
          })

          regionData.bad_servers?.forEach(server => {
            rows.push({
              app: appName,
              region,
              serverName: server.name,
              imageName: server.image_name,
              imageId: server.image_id,
              status: 'non-compliant',
              imageYear: server.image_year,
              imageWeek: server.image_week,
              reason: server.reason || 'Unknown'
            })
          })
        }
      })
    }

    return rows
  }, [data])

  // Get unique values for filters
  const uniqueRegions = [...new Set(tableData.map(row => row.region))]
  const uniqueApps = [...new Set(tableData.map(row => row.app))]

  // Filter and sort data
  const filteredAndSortedData = useMemo(() => {
    let filtered = tableData.filter(row => {
      const matchesSearch = 
        row.serverName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        row.imageName.toLowerCase().includes(searchTerm.toLowerCase())
      
      const matchesStatus = 
        filterStatus === 'all' || row.status === filterStatus
      
      const matchesRegion = 
        filterRegion === 'all' || row.region === filterRegion
      
      const matchesApp = 
        filterApp === 'all' || row.app === filterApp

      return matchesSearch && matchesStatus && matchesRegion && matchesApp
    })

    // Sort
    if (sortConfig.key) {
      filtered.sort((a, b) => {
        const aVal = a[sortConfig.key]
        const bVal = b[sortConfig.key]
        
        if (aVal < bVal) return sortConfig.direction === 'asc' ? -1 : 1
        if (aVal > bVal) return sortConfig.direction === 'asc' ? 1 : -1
        return 0
      })
    }

    return filtered
  }, [tableData, searchTerm, filterStatus, filterRegion, filterApp, sortConfig])

  const handleSort = (key) => {
    setSortConfig(prev => ({
      key,
      direction: prev.key === key && prev.direction === 'asc' ? 'desc' : 'asc'
    }))
  }

  const SortIcon = ({ columnKey }) => {
    if (sortConfig.key !== columnKey) return null
    return sortConfig.direction === 'asc' ? 
      <ChevronUp className="w-4 h-4 inline ml-1" /> : 
      <ChevronDown className="w-4 h-4 inline ml-1" />
  }

  return (
    <div className="card">
      <div className="mb-6">
        <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">
          Server Details
        </h2>

        {/* Filters */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
          {/* Search */}
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
            <input
              type="text"
              placeholder="Search servers or images..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            />
          </div>

          {/* Status Filter */}
          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          >
            <option value="all">All Status</option>
            <option value="compliant">Compliant</option>
            <option value="non-compliant">Non-Compliant</option>
          </select>

          {/* Region Filter */}
          <select
            value={filterRegion}
            onChange={(e) => setFilterRegion(e.target.value)}
            className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          >
            <option value="all">All Regions</option>
            {uniqueRegions.map(region => (
              <option key={region} value={region}>{region}</option>
            ))}
          </select>

          {/* App Filter */}
          {uniqueApps.length > 1 && (
            <select
              value={filterApp}
              onChange={(e) => setFilterApp(e.target.value)}
              className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            >
              <option value="all">All Apps</option>
              {uniqueApps.map(app => (
                <option key={app} value={app}>{app}</option>
              ))}
            </select>
          )}
        </div>

        <p className="text-sm text-gray-500 dark:text-gray-400">
          Showing {filteredAndSortedData.length} of {tableData.length} servers
        </p>
      </div>

      {/* Table */}
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
          <thead className="bg-gray-50 dark:bg-gray-800">
            <tr>
              {uniqueApps.length > 1 && (
                <th 
                  onClick={() => handleSort('app')}
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700"
                >
                  App <SortIcon columnKey="app" />
                </th>
              )}
              <th 
                onClick={() => handleSort('region')}
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700"
              >
                Region <SortIcon columnKey="region" />
              </th>
              <th 
                onClick={() => handleSort('serverName')}
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700"
              >
                Server Name <SortIcon columnKey="serverName" />
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Image Name
              </th>
              <th 
                onClick={() => handleSort('status')}
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700"
              >
                Status <SortIcon columnKey="status" />
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Image Week
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Reason
              </th>
            </tr>
          </thead>
          <tbody className="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
            {filteredAndSortedData.map((row, idx) => (
              <tr key={idx} className="hover:bg-gray-50 dark:hover:bg-gray-800">
                {uniqueApps.length > 1 && (
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100">
                    {row.app}
                  </td>
                )}
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100">
                  <span className="badge-warning">{row.region}</span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-gray-100">
                  {row.serverName}
                </td>
                <td className="px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
                  <div className="max-w-xs truncate" title={row.imageName}>
                    {row.imageName}
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm">
                  {row.status === 'compliant' ? (
                    <span className="badge-success">Compliant</span>
                  ) : (
                    <span className="badge-danger">Non-Compliant</span>
                  )}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                  {row.imageYear && row.imageWeek ? 
                    `${row.imageYear}-W${row.imageWeek.toString().padStart(2, '0')}` : 
                    'N/A'
                  }
                </td>
                <td className="px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
                  {row.reason}
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {filteredAndSortedData.length === 0 && (
          <div className="text-center py-12">
            <p className="text-gray-500 dark:text-gray-400">
              No servers found matching your filters
            </p>
          </div>
        )}
      </div>
    </div>
  )
}

export default ComplianceTable
