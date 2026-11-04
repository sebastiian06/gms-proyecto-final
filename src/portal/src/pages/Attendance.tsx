import { useEffect, useState } from 'react'
import { apiClient } from '../api/client'
import type { AttendanceRecord, Member, Page } from '../types'
import { AxiosError } from 'axios'
import { RefreshCw, Search } from 'lucide-react'

export function Attendance() {
  const [records, setRecords] = useState<AttendanceRecord[]>([])
  const [members, setMembers] = useState<Member[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [search, setSearch] = useState('')

  async function load() {
    setLoading(true)
    setError(null)
    try {
      const [attRes, memRes] = await Promise.all([
        apiClient.get<AttendanceRecord[]>('/attendance'),
        apiClient.get<Page<Member>>('/members', { params: { page: 0, size: 1000 } })
      ])
      setRecords(attRes.data)
      setMembers(memRes.data.content)
    } catch (err) {
      const axiosErr = err as AxiosError<{ detail?: string }>
      setError(axiosErr.response?.data?.detail ?? 'Failed to load attendance')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  function formatDateTime(iso: string) {
    return new Date(iso).toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  // Filtramos los registros de asistencia basándonos en la búsqueda
  const filteredRecords = records.filter(r => {
    const member = members.find(m => m.id === r.memberId)
    if (!member) return false
    return (
      member.fullName.toLowerCase().includes(search.toLowerCase()) ||
      member.documentNumber.includes(search)
    )
  })

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Attendance</h1>
          <p className="text-slate-500 text-sm mt-1">Last 30 days of check-ins and check-outs.</p>
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

      <div className="mb-4">
        <div className="relative max-w-md">
          <Search className="absolute left-3 top-2.5 text-slate-400" size={16} />
          <input
            type="text"
            placeholder="Search by name or document..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full border border-slate-300 rounded-lg pl-9 pr-3 py-2 text-sm focus:ring-2 focus:ring-blue-500 outline-none"
          />
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-sm overflow-hidden border border-slate-200">
        <table className="w-full">
          <thead className="bg-slate-50 border-b border-slate-200">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">Name</th>
              <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">Document</th>
              <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">Date & time</th>
              <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">Direction</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200">
            {filteredRecords.length === 0 && (
              <tr>
                <td colSpan={4} className="px-4 py-8 text-center text-slate-500">
                  No attendance records found
                </td>
              </tr>
            )}
            {filteredRecords.map((r) => {
              const m = members.find(x => x.id === r.memberId)
              return (
                <tr key={r.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3 text-sm font-medium text-slate-800">
                    {m ? m.fullName : 'Unknown'}
                  </td>
                  <td className="px-4 py-3 text-sm text-slate-600">
                    {m ? m.documentNumber : `ID: ${r.memberId}`}
                  </td>
                  <td className="px-4 py-3 text-sm text-slate-600">{formatDateTime(r.timestamp)}</td>
                  <td className="px-4 py-3 text-sm">
                    <span
                      className={`px-2 py-1 rounded-full text-xs font-semibold ${
                        r.direction === 'IN'
                          ? 'bg-green-100 text-green-700'
                          : 'bg-amber-100 text-amber-700'
                      }`}
                    >
                      {r.direction}
                    </span>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    </div>
  )
}