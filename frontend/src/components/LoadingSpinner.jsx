function LoadingSpinner() {
  return (
    <div className="flex items-center justify-center py-12">
      <div className="relative">
        <div className="w-16 h-16 border-4 border-primary-200 dark:border-primary-900 rounded-full"></div>
        <div className="w-16 h-16 border-4 border-primary-600 border-t-transparent rounded-full animate-spin absolute top-0 left-0"></div>
      </div>
      <span className="ml-4 text-gray-600 dark:text-gray-400 text-lg">
        Loading compliance data...
      </span>
    </div>
  )
}

export default LoadingSpinner
