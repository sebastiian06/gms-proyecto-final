import { useEffect, useState } from 'react'
import { useAuth } from '../auth/AuthContext'
import { apiClient } from '../api/client'
import { AxiosError } from 'axios'
import {
  Users as UsersIcon,
  DollarSign,
  Calendar,
  Package,
  RefreshCw,
} from 'lucide-react'

interface Summary {
  activeMembers: number
  monthlyRevenue: number
  attendanceThisMonth: number
  mostSoldPlan: string
}

interface RecentPayment {
  id: number
  memberName: string
  planName: string
  amount: number
  method: string
  recordedDate: string
}

export function Dashboard() {
  const { user } = useAuth()
  const [summary, setSummary] = useState<Summary | null>(null)
  const [payments, setPayments] = useState<RecentPayment[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      const [summaryRes, paymentsRes] = await Promise.all([
        apiClient.get<Summary>('/reports/summary'),
        apiClient.get<RecentPayment[]>('/reports/recent-payments', { params: { limit: 5 } }),
      ])
      setSummary(summaryRes.data)
      setPayments(paymentsRes.data)
    } catch (err) {
      const axiosErr = err as AxiosError<{ detail?: string }>
      setError(axiosErr.response?.data?.detail ?? 'Failed to load dashboard')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  function greeting() {
    const hour = new Date().getHours()
    if (hour < 12) return 'Good morning'
    if (hour < 18) return 'Good afternoon'
    return 'Good evening'
  }

  function formatMoney(amount: number) {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      maximumFractionDigits: 0,
    }).format(amount)
  }

  return (
    <div>
      <div className="flex justify-between items-start mb-8">
        <div>
          <p className="text-sm text-blue-600 font-medium">{user?.role}</p>
          <h1 className="text-3xl font-bold text-slate-800 mt-1">
            {greeting()}, {user?.username}
          </h1>
          <p className="text-slate-500 mt-1">Here's what's happening at your gym today.</p>
        </div>
        <button
          onClick={load}
          disabled={loading}
          className="flex items-center gap-2 px-4 py-2 border border-slate-300 rounded-lg hover:bg-slate-50 disabled:opacity-50 text-sm"
        >
          <RefreshCw size={16} className={loading ? 'animate-spin' : ''} /> Refresh
        </button>
      </div>

      {error && <div className="bg-red-100 text-red-700 p-3 rounded-lg mb-4">{error}</div>}

      {/* 4 Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <MetricCard
          label="Active members"
          value={summary?.activeMembers ?? '—'}
          icon={UsersIcon}
          color="blue"
        />
        <MetricCard
          label="Monthly revenue"
          value={summary ? formatMoney(summary.monthlyRevenue) : '—'}
          icon={DollarSign}
          color="green"
        />
        <MetricCard
          label="Attendance this month"
          value={summary?.attendanceThisMonth ?? '—'}
          icon={Calendar}
          color="amber"
        />
        <MetricCard
          label="Most-sold plan"
          value={summary?.mostSoldPlan ?? '—'}
          icon={Package}
          color="purple"
        />
      </div>

      {/* Recent payments */}
      <div className="bg-white rounded-xl shadow-sm overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-200">
          <h2 className="font-semibold text-slate-800">Recent payments</h2>
        </div>
        <table className="w-full">
          <thead className="bg-slate-50">
            <tr>
              <th className="px-6 py-2 text-left text-xs font-semibold text-slate-500 uppercase">Member</th>
              <th className="px-6 py-2 text-left text-xs font-semibold text-slate-500 uppercase">Plan</th>
              <th className="px-6 py-2 text-left text-xs font-semibold text-slate-500 uppercase">Amount</th>
              <th className="px-6 py-2 text-left text-xs font-semibold text-slate-500 uppercase">Method</th>
              <th className="px-6 py-2 text-left text-xs font-semibold text-slate-500 uppercase">Date</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200">
            {payments.length === 0 && (
              <tr>
                <td colSpan={5} className="px-6 py-8 text-center text-slate-500">
                  No payments yet
                </td>
              </tr>
            )}
            {payments.map((p) => (
              <tr key={p.id}>
                <td className="px-6 py-3 text-sm font-medium">{p.memberName}</td>
                <td className="px-6 py-3 text-sm">{p.planName}</td>
                <td className="px-6 py-3 text-sm font-semibold">{formatMoney(p.amount)}</td>
                <td className="px-6 py-3 text-sm">
                  <span className="px-2 py-0.5 bg-slate-100 rounded text-xs">{p.method}</span>
                </td>
                <td className="px-6 py-3 text-sm text-slate-500">{p.recordedDate}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

interface MetricCardProps {
  label: string
  value: string | number
  icon: React.ComponentType<{ size?: number }>
  color: 'blue' | 'green' | 'amber' | 'purple'
}

function MetricCard({ label, value, icon: Icon, color }: MetricCardProps) {
  const colorClasses = {
    blue: 'bg-blue-100 text-blue-600',
    green: 'bg-green-100 text-green-600',
    amber: 'bg-amber-100 text-amber-600',
    purple: 'bg-purple-100 text-purple-600',
  }

  return (
    <div className="bg-white rounded-xl p-6 shadow-sm">
      <div className="flex justify-between items-start">
        <p className="text-sm text-slate-500">{label}</p>
        <div className={`p-2 rounded-lg ${colorClasses[color]}`}>
          <Icon size={18} />
        </div>
      </div>
      <p className="text-3xl font-bold text-slate-800 mt-2">{value}</p>
    </div>
  )
}