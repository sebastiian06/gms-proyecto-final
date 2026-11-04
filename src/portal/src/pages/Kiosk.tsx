import { useState, type FormEvent } from 'react'
import { apiClient } from '../api/client'
import { AxiosError } from 'axios'
import { LogIn, LogOut, CheckCircle, XCircle } from 'lucide-react'

type Mode = 'IN' | 'OUT'

interface KioskResult {
  type: 'success' | 'error'
  message: string
  memberName?: string
  direction?: Mode
}

export function Kiosk() {
  const [code, setCode] = useState('')
  const [mode, setMode] = useState<Mode>('IN')
  const [result, setResult] = useState<KioskResult | null>(null)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!code.trim()) return
    setLoading(true)
    setResult(null)
    try {
      const endpoint = mode === 'IN' ? '/attendance/entries' : '/attendance/exits'
      await apiClient.post(endpoint, { accessCode: code.trim() })
      setResult({
        type: 'success',
        message: mode === 'IN' ? 'Welcome!' : 'Goodbye!',
        direction: mode,
      })
      setCode('')
      setTimeout(() => setResult(null), 4000)
    } catch (err) {
      const axiosErr = err as AxiosError<{ detail?: string; title?: string }>
      setResult({
        type: 'error',
        message:
          axiosErr.response?.data?.detail ??
          axiosErr.response?.data?.title ??
          'Something went wrong',
      })
      setTimeout(() => setResult(null), 4000)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-slate-900 text-white flex flex-col items-center justify-center p-6">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="w-16 h-16 rounded-2xl bg-blue-600 flex items-center justify-center font-bold text-2xl mx-auto mb-4">
            G
          </div>
          <h1 className="text-3xl font-bold">GMS Kiosk</h1>
          <p className="text-slate-400 mt-1">Enter your access code to check in or out</p>
        </div>

        {/* Mode toggle */}
        <div className="grid grid-cols-2 gap-2 mb-6">
          <button
            type="button"
            onClick={() => setMode('IN')}
            className={`flex items-center justify-center gap-2 py-3 rounded-lg font-medium transition-colors ${
              mode === 'IN'
                ? 'bg-green-600 text-white'
                : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
            }`}
          >
            <LogIn size={18} /> Check In
          </button>
          <button
            type="button"
            onClick={() => setMode('OUT')}
            className={`flex items-center justify-center gap-2 py-3 rounded-lg font-medium transition-colors ${
              mode === 'OUT'
                ? 'bg-amber-600 text-white'
                : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
            }`}
          >
            <LogOut size={18} /> Check Out
          </button>
        </div>

        {/* Result */}
        {result && (
          <div
            className={`mb-4 p-4 rounded-lg flex items-start gap-3 ${
              result.type === 'success'
                ? 'bg-green-600/20 border border-green-500 text-green-100'
                : 'bg-red-600/20 border border-red-500 text-red-100'
            }`}
          >
            {result.type === 'success' ? <CheckCircle size={20} /> : <XCircle size={20} />}
            <div>
              <p className="font-semibold">{result.message}</p>
              {result.direction && (
                <p className="text-sm opacity-80">
                  {result.direction === 'IN' ? 'Checked in' : 'Checked out'}
                </p>
              )}
            </div>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="bg-white rounded-2xl p-6 shadow-2xl">
          <label className="block">
            <span className="block text-sm font-medium text-slate-700 mb-2">
              Access code
            </span>
            <input
              type="text"
              inputMode="numeric"
              pattern="[0-9]*"
              maxLength={6}
              value={code}
              onChange={(e) => setCode(e.target.value.replace(/\D/g, ''))}
              autoFocus
              className="w-full text-center text-4xl tracking-[0.5em] font-bold border-2 border-slate-300 rounded-lg px-4 py-4 text-slate-800 focus:outline-none focus:border-blue-500"
              placeholder="----"
            />
          </label>
          <button
            type="submit"
            disabled={loading || !code.trim()}
            className="mt-4 w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-lg disabled:opacity-50"
          >
            {loading ? 'Processing...' : mode === 'IN' ? 'Register entry' : 'Register exit'}
          </button>
        </form>

        <p className="text-center text-xs text-slate-500 mt-6">
          If you don't have an access code, ask the receptionist.
        </p>
      </div>
    </div>
  )
}