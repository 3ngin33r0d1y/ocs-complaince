import { Chart as ChartJS, ArcElement, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend } from 'chart.js'
import { Pie, Bar } from 'react-chartjs-2'

ChartJS.register(ArcElement, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

function ComplianceCharts({ data }) {
  // Prepare data for charts
  const prepareChartData = () => {
    const regionStats = {}
    const appStats = {}

    if (data.apps) {
      // Multiple apps
      Object.entries(data.apps).forEach(([appName, appData]) => {
        let appCompliant = 0
        let appNonCompliant = 0

        Object.entries(appData.regions || {}).forEach(([region, regionData]) => {
          if (!regionData.error) {
            // Region stats
            if (!regionStats[region]) {
              regionStats[region] = { compliant: 0, nonCompliant: 0 }
            }
            regionStats[region].compliant += regionData.compliant || 0
            regionStats[region].nonCompliant += regionData.non_compliant || 0

            // App stats
            appCompliant += regionData.compliant || 0
            appNonCompliant += regionData.non_compliant || 0
          }
        })

        appStats[appName] = { compliant: appCompliant, nonCompliant: appNonCompliant }
      })
    } else if (data.regions) {
      // Single app
      Object.entries(data.regions).forEach(([region, regionData]) => {
        if (!regionData.error) {
          regionStats[region] = {
            compliant: regionData.compliant || 0,
            nonCompliant: regionData.non_compliant || 0
          }
        }
      })
    }

    return { regionStats, appStats }
  }

  const { regionStats, appStats } = prepareChartData()

  // Overall compliance pie chart
  const totalCompliant = Object.values(regionStats).reduce((sum, r) => sum + r.compliant, 0)
  const totalNonCompliant = Object.values(regionStats).reduce((sum, r) => sum + r.nonCompliant, 0)

  const pieData = {
    labels: ['Compliant', 'Non-Compliant'],
    datasets: [
      {
        data: [totalCompliant, totalNonCompliant],
        backgroundColor: [
          'rgba(34, 197, 94, 0.8)',
          'rgba(239, 68, 68, 0.8)',
        ],
        borderColor: [
          'rgba(34, 197, 94, 1)',
          'rgba(239, 68, 68, 1)',
        ],
        borderWidth: 2,
      },
    ],
  }

  const pieOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          color: '#9CA3AF',
          font: {
            size: 12
          }
        }
      },
      title: {
        display: true,
        text: 'Overall Compliance Distribution',
        color: '#F3F4F6',
        font: {
          size: 16,
          weight: 'bold'
        }
      },
    },
  }

  // Region comparison bar chart
  const regionLabels = Object.keys(regionStats)
  const barData = {
    labels: regionLabels,
    datasets: [
      {
        label: 'Compliant',
        data: regionLabels.map(region => regionStats[region].compliant),
        backgroundColor: 'rgba(34, 197, 94, 0.8)',
        borderColor: 'rgba(34, 197, 94, 1)',
        borderWidth: 1,
      },
      {
        label: 'Non-Compliant',
        data: regionLabels.map(region => regionStats[region].nonCompliant),
        backgroundColor: 'rgba(239, 68, 68, 0.8)',
        borderColor: 'rgba(239, 68, 68, 1)',
        borderWidth: 1,
      },
    ],
  }

  const barOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          color: '#9CA3AF',
          font: {
            size: 12
          }
        }
      },
      title: {
        display: true,
        text: 'Compliance by Region',
        color: '#F3F4F6',
        font: {
          size: 16,
          weight: 'bold'
        }
      },
    },
    scales: {
      x: {
        ticks: {
          color: '#9CA3AF'
        },
        grid: {
          color: 'rgba(156, 163, 175, 0.1)'
        }
      },
      y: {
        ticks: {
          color: '#9CA3AF'
        },
        grid: {
          color: 'rgba(156, 163, 175, 0.1)'
        }
      }
    }
  }

  // App comparison bar chart (if multiple apps)
  const appBarData = Object.keys(appStats).length > 0 ? {
    labels: Object.keys(appStats),
    datasets: [
      {
        label: 'Compliant',
        data: Object.values(appStats).map(app => app.compliant),
        backgroundColor: 'rgba(34, 197, 94, 0.8)',
        borderColor: 'rgba(34, 197, 94, 1)',
        borderWidth: 1,
      },
      {
        label: 'Non-Compliant',
        data: Object.values(appStats).map(app => app.nonCompliant),
        backgroundColor: 'rgba(239, 68, 68, 0.8)',
        borderColor: 'rgba(239, 68, 68, 1)',
        borderWidth: 1,
      },
    ],
  } : null

  const appBarOptions = {
    ...barOptions,
    plugins: {
      ...barOptions.plugins,
      title: {
        ...barOptions.plugins.title,
        text: 'Compliance by Application'
      }
    }
  }

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      {/* Pie Chart */}
      <div className="card">
        <div style={{ height: '300px' }}>
          <Pie data={pieData} options={pieOptions} />
        </div>
      </div>

      {/* Region Bar Chart */}
      <div className="card">
        <div style={{ height: '300px' }}>
          <Bar data={barData} options={barOptions} />
        </div>
      </div>

      {/* App Bar Chart (if multiple apps) */}
      {appBarData && (
        <div className="card lg:col-span-2">
          <div style={{ height: '300px' }}>
            <Bar data={appBarData} options={appBarOptions} />
          </div>
        </div>
      )}
    </div>
  )
}

export default ComplianceCharts
