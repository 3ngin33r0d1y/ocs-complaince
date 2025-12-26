import { useState } from 'react'
import { ShieldCheck } from 'lucide-react'

function LoginPage({ onLogin }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = (event) => {
    event.preventDefault()
    if (!email || !password) {
      setError('Enter your email and password to continue.')
      return
    }
    if (email !== 'admin@devops.com' || password !== 'Devops@team') {
      setError('Invalid email or password.')
      return
    }
    setError('')
    onLogin(email)
  }

  return (
    <div className="relative min-h-screen overflow-hidden bg-[#0b0f1a] text-white">
      <div className="pointer-events-none absolute inset-0">
        <div className="absolute -top-24 right-10 h-72 w-72 rounded-full bg-gradient-to-br from-amber-400/40 via-orange-500/30 to-transparent blur-3xl" />
        <div className="absolute bottom-0 left-0 h-96 w-96 rounded-full bg-gradient-to-tr from-emerald-400/30 via-cyan-400/20 to-transparent blur-3xl" />
        <div className="absolute inset-x-0 top-32 mx-auto h-80 w-80 rounded-full border border-white/10" />
      </div>

      <div className="relative mx-auto flex min-h-screen w-full max-w-6xl flex-col items-center justify-center px-6 py-16 lg:flex-row lg:justify-between lg:gap-12">
        <section className="mb-12 max-w-lg text-left lg:mb-0">
          <div className="inline-flex items-center gap-3 rounded-full border border-white/15 bg-white/5 px-4 py-2 text-sm uppercase tracking-[0.2em] text-white/70">
            <ShieldCheck className="h-4 w-4 text-emerald-300" />
            Compliance Access
          </div>
          <h1 className="mt-6 font-display text-4xl leading-tight text-white sm:text-5xl">
            Welcome back to your compliance command center.
          </h1>
          <p className="mt-4 text-base text-white/70 sm:text-lg">
            Monitor drift, validate regional posture, and keep every app within spec.
            This portal is a quick checkpoint before you enter the live dashboard.
          </p>
          <div className="mt-8 grid gap-4 text-sm text-white/60 sm:grid-cols-2">
            <div className="rounded-2xl border border-white/10 bg-white/5 px-4 py-4">
              <p className="font-semibold text-white">Instant status</p>
              <p className="mt-2">Access compliance data in one click after sign-in.</p>
            </div>
            <div className="rounded-2xl border border-white/10 bg-white/5 px-4 py-4">
              <p className="font-semibold text-white">Secure by default</p>
              <p className="mt-2">Session stays local and clears when you sign out.</p>
            </div>
          </div>
        </section>

        <section className="w-full max-w-md rounded-3xl border border-white/10 bg-white/10 p-8 shadow-2xl backdrop-blur">
          <div className="flex items-center justify-between">
            <h2 className="font-display text-2xl">Sign in</h2>
            <span className="rounded-full border border-white/10 bg-white/10 px-3 py-1 text-xs uppercase tracking-[0.2em] text-white/60">
              OCS
            </span>
          </div>

          <form onSubmit={handleSubmit} className="mt-8 space-y-5">
            <label className="block text-sm">
              <span className="text-white/70">Email address</span>
              <input
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                className="mt-2 w-full rounded-2xl border border-white/10 bg-white/5 px-4 py-3 text-white placeholder:text-white/40 focus:border-emerald-300 focus:outline-none focus:ring-2 focus:ring-emerald-300/40"
                placeholder="you@company.com"
              />
            </label>

            <label className="block text-sm">
              <span className="text-white/70">Password</span>
              <div className="mt-2 flex items-center rounded-2xl border border-white/10 bg-white/5 px-4 py-3 focus-within:border-amber-300 focus-within:ring-2 focus-within:ring-amber-300/40">
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  className="w-full bg-transparent text-white placeholder:text-white/40 focus:outline-none"
                  placeholder="Enter your password"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((value) => !value)}
                  className="text-xs uppercase tracking-[0.2em] text-white/60"
                >
                  {showPassword ? 'Hide' : 'Show'}
                </button>
              </div>
            </label>

            {error && (
              <p className="rounded-2xl border border-amber-200/30 bg-amber-200/10 px-4 py-3 text-sm text-amber-100">
                {error}
              </p>
            )}

            <button
              type="submit"
              className="w-full rounded-2xl bg-gradient-to-r from-emerald-400 via-cyan-400 to-sky-400 px-4 py-3 text-sm font-semibold uppercase tracking-[0.2em] text-[#0b0f1a] transition hover:brightness-110"
            >
              Continue to dashboard
            </button>

            <div className="flex items-center justify-between text-xs text-white/50">
              <span>No account? Use any credentials.</span>
              <button type="button" className="text-white/70 hover:text-white">
                Need help?
              </button>
            </div>
          </form>
        </section>
      </div>
    </div>
  )
}

export default LoginPage
