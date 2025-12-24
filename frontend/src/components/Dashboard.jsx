import { CheckCircle, XCircle, Server, TrendingUp } from 'lucide-react'

function Dashboard({ data }) {
  // Calculate overall statistics
  const calculateStats = () => {
    let totalServers = 0
    let totalCompliant = 0
    let totalNonCompliant = 0

    if (data.apps) {
      // Multiple apps
      Object.values(data.apps).forEach(app => {
        Object.values(app.regions || {}).forEach(region => {
          if (!region.error) {
            totalServers += region.total_servers || 0
            totalCompliant += region.compliant || 0
            totalNonCompliant += region.non_compliant || 0
          }
        })
      })
    } else if (data.regions) {
      // Single app
      Object.values(data.regions).forEach(region => {
        if (!region.error) {
          totalServers += region.total_servers || 0
          totalCompliant += region.compliant || 0
          totalNonCompliant += region.non_compliant || 0
        }
      })
    }

    const compliancePercentage = totalServers > 0 
      ? ((totalCompliant / totalServers) * 100).toFixed(2)
      : 0

    return {
      totalServers,
      totalCompliant,
      totalNonCompliant,
      compliancePercentage
    }
  }

  const stats = calculateStats()

  const StatCard = ({ title, value, icon: Icon, color, subtitle }) => (
    <div className="card">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium text-gray-600 dark:text-gray-400">
            {title}
          </p>
          <p className={`text-3xl font-bold mt-2 ${color}`}>
            {value}
          </p>
          {subtitle && (
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
              {subtitle}
            </p>
          )}
        </div>
        <div className={`p-3 rounded-full ${color.replace('text-', 'bg-').replace('600', '100')} dark:${color.replace('text-', 'bg-').replace('600', '900')}`}>
          <Icon className={`w-6 h-6 ${color}`} />
        </div>
      </div>
    </div>
  )

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <StatCard
        title="Total Servers"
        value={stats.totalServers}
        icon={Server}
        color="text-blue-600 dark:text-blue-400"
        subtitle="Across all regions"
      />
      <StatCard
        title="Compliant"
        value={stats.totalCompliant}
        icon={CheckCircle}
        color="text-green-600 dark:text-green-400"
        subtitle="Current week builds"
      />
      <StatCard
        title="Non-Compliant"
        value={stats.totalNonCompliant}
        icon={XCircle}
        color="text-red-600 dark:text-red-400"
        subtitle="Older or unparsable"
      />
      <StatCard
        title="Compliance Rate"
        value={`${stats.compliancePercentage}%`}
        icon={TrendingUp}
        color="text-purple-600 dark:text-purple-400"
        subtitle={`Week ${data.current_week || 'N/A'}, ${data.current_year || ''}`}
      />
    </div>
  )
}

export default Dashboard
