import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { apiClient } from '../api/client'
import type { Member, Page, Membership, Plan } from '../types'
import { MemberFormModal } from '../components/MemberFormModal'
import { AxiosError } from 'axios'
import { Plus, Search, RefreshCw } from 'lucide-react'

export function Members() {
  const [data, setData] = useState<Page<Member> | null>(null)
  const [memberships, setMemberships] = useState<Membership[]>([])
  const [plans, setPlans] = useState<Plan[]>([])
  
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState<Member | null>(null)

  // Filtros
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('')

  async function load() {
    setLoading(true)
    setError(null)
    try {
      // Traemos miembros, membresías y planes en paralelo
      const [memRes, memshipsRes, plansRes] = await Promise.all([
        apiClient.get<Page<Member>>('/members', {
          params: { page, size: 20, active: statusFilter === 'active' ? true : statusFilter === 'inactive' ? false : undefined }
        }),
        apiClient.get<Page<Membership>>('/memberships', { params: { page: 0, size: 1000, status: 'ACTIVE' } }),
        apiClient.get<Page<Plan>>('/plans', { params: { page: 0, size: 100 } })
      ])
      
      setData(memRes.data)
      setMemberships(memshipsRes.data.content)
      setPlans(plansRes.data.content)
    } catch (err) {
      const axiosErr = err as AxiosError<{ detail?: string; title?: string }>
      setError(axiosErr.response?.data?.detail ?? 'Failed to load members')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [page, statusFilter])

  async function handleDeactivate(id: number) {
    if (!confirm('Deactivate this member?')) return
    try {
      await apiClient.delete(`/members/${id}`)
      load()
    } catch (err) {
      const axiosErr = err as AxiosError<{ detail?: string }>
      alert(axiosErr.response?.data?.detail ?? 'Failed to deactivate member')
    }
  }

  async function handleActivate(id: number) {
    try {
      await apiClient.patch(`/members/${id}/activate`)
      load()
    } catch (err) {
      const axiosErr = err as AxiosError<{ detail?: string }>
      alert(axiosErr.response?.data?.detail ?? 'Failed to activate member')
    }
  }

  async function handleRegenerateCode(id: number) {
    if (!confirm('Regenerate this member\'s access code? The old code will stop working.')) return
    try {
      await apiClient.post(`/members/${id}/regenerate-access-code`)
      load() // Recargamos para ver el nuevo código
    } catch (err) {
      const axiosErr = err as AxiosError<{ detail?: string }>
      alert(axiosErr.response?.data?.detail ?? 'Failed to regenerate code')
    }
  }

  // Helpers para cruzar datos
  function getPlanName(memberId: number) {
    // Filtramos todas las membresías de este miembro que estén activas
    const activeMemships = memberships.filter(m => m.memberId === memberId && m.status === 'ACTIVE')
    
    if (activeMemships.length === 0) return '—'
    
    // Si hay varias, tomamos la que tenga el ID más alto (la más reciente)
    const latestMemship = activeMemships.sort((a, b) => b.id - a.id)[0]
    
    const plan = plans.find(p => p.id === latestMemship.planId)
    return plan ? plan.name : '—'
  }

  // Filtro local por nombre (búsqueda)
  const filteredContent = data?.content.filter(m => 
    m.fullName.toLowerCase().includes(search.toLowerCase()) || 
    m.documentNumber.includes(search)
  ) || []

  return (
    <div>
      <div className="mb-6 flex justify-between items-start">
        <div>
          <p className="text-xs font-bold text-blue-600 uppercase tracking-wider mb-1">Member Management</p>
          <h1 className="text-3xl font-bold text-slate-800">Members</h1>
          <p className="text-slate-500 mt-1">Manage member profiles, status and Memberships.</p>
        </div>
        <button
          onClick={() => { setEditing(null); setShowForm(true) }}
          className="flex items-center gap-2 bg-blue-700 hover:bg-blue-800 text-white px-5 py-2.5 rounded-lg font-medium shadow-sm transition-colors"
        >
          <Plus size={18} /> New member
        </button>
      </div>

      {error && <div className="bg-red-100 text-red-700 p-3 rounded-lg mb-4">{error}</div>}

      <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
        {/* Filtros */}
        <div className="p-4 border-b border-slate-200 flex gap-4 bg-slate-50/50">
          <div className="flex-1">
            <label className="block text-xs font-semibold text-slate-600 mb-1">Search members</label>
            <div className="relative">
              <Search className="absolute left-3 top-2.5 text-slate-400" size={16} />
              <input
                type="text"
                placeholder="Search by name..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="w-full border border-slate-300 rounded-lg pl-9 pr-3 py-2 text-sm focus:ring-2 focus:ring-blue-500 outline-none"
              />
            </div>
          </div>
          <div className="w-48">
            <label className="block text-xs font-semibold text-slate-600 mb-1">Status</label>
            <select
              value={statusFilter}
              onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
              className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500 outline-none"
            >
              <option value="">All</option>
              <option value="active">Active</option>
              <option value="inactive">Inactive</option>
            </select>
          </div>
        </div>

        {/* Tabla */}
        <table className="w-full">
          <thead className="bg-white border-b border-slate-200">
            <tr>
              <th className="px-6 py-4 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">Document number</th>
              <th className="px-6 py-4 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">Full name</th>
              <th className="px-6 py-4 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">Contact</th>
              <th className="px-6 py-4 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">Plan</th>
              <th className="px-6 py-4 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">Access Code</th>
              <th className="px-6 py-4 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">Status</th>
              <th className="px-6 py-4 text-right text-xs font-semibold text-slate-500 uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {loading && <tr><td colSpan={7} className="text-center py-8 text-slate-500">Loading...</td></tr>}
            
            {!loading && filteredContent.length === 0 && (
              <tr><td colSpan={7} className="text-center py-8 text-slate-500">No members found</td></tr>
            )}

            {filteredContent.map((m) => (
              <tr key={m.id} className="hover:bg-slate-50">
                <td className="px-6 py-4 text-sm text-slate-600">{m.documentNumber}</td>
                <td className="px-6 py-4 text-sm font-medium text-slate-800">{m.fullName}</td>
                <td className="px-6 py-4 text-sm text-slate-600">{m.contact}</td>
                <td className="px-6 py-4 text-sm text-slate-600">{getPlanName(m.id)}</td>
                
                {/* ACCESS CODE CON BOTÓN DE REGENERAR */}
                <td className="px-6 py-4 text-sm">
                  {m.accessCode ? (
                    <div className="flex items-center gap-2">
                      <span className="font-mono font-bold text-blue-600 bg-blue-50 px-2 py-1 rounded border border-blue-100">
                        {m.accessCode}
                      </span>
                      <button
                        onClick={() => handleRegenerateCode(m.id)}
                        className="text-slate-400 hover:text-blue-600 transition-colors"
                        title="Regenerate access code"
                      >
                        <RefreshCw size={14} />
                      </button>
                    </div>
                  ) : (
                    <span className="text-slate-400 text-xs">—</span>
                  )}
                </td>

                <td className="px-6 py-4 text-sm">
                  {m.active ? (
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-50 text-green-700 border border-green-200">
                      Active
                    </span>
                  ) : (
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-50 text-red-700 border border-red-200">
                      Inactive
                    </span>
                  )}
                </td>
                <td className="px-6 py-4 text-right space-x-2">
                  <Link
                    to={`/members/${m.id}`}
                    className="inline-flex items-center justify-center px-4 py-1.5 border border-slate-300 rounded-lg text-sm font-medium text-slate-700 bg-white hover:bg-slate-50 transition-colors"
                  >
                    View
                  </Link>
                  <button
                    onClick={() => { setEditing(m); setShowForm(true) }}
                    className="inline-flex items-center justify-center px-4 py-1.5 border border-slate-300 rounded-lg text-sm font-medium text-slate-700 bg-white hover:bg-slate-50 transition-colors"
                  >
                    Edit
                  </button>
                  {m.active ? (
                    <button
                      onClick={() => handleDeactivate(m.id)}
                      className="inline-flex items-center justify-center px-4 py-1.5 rounded-lg text-sm font-medium text-white bg-red-600 hover:bg-red-700 transition-colors shadow-sm"
                    >
                      Deactivate
                    </button>
                  ) : (
                    <button
                      onClick={() => handleActivate(m.id)}
                      className="inline-flex items-center justify-center px-4 py-1.5 rounded-lg text-sm font-medium text-white bg-green-600 hover:bg-green-700 transition-colors shadow-sm"
                    >
                      Activate
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        
        {/* Paginación */}
        {data && data.page.totalPages > 1 && (
          <div className="px-6 py-4 border-t border-slate-200 flex justify-between items-center">
            <button
              onClick={() => setPage(p => Math.max(0, p - 1))}
              disabled={page === 0}
              className="px-4 py-2 border border-slate-300 rounded-lg text-sm font-medium text-slate-700 disabled:opacity-50"
            >
              Previous
            </button>
            <span className="text-sm text-slate-500">Page {page + 1} of {data.page.totalPages}</span>
            <button
              onClick={() => setPage(p => p + 1)}
              disabled={page >= data.page.totalPages - 1}
              className="px-4 py-2 border border-slate-300 rounded-lg text-sm font-medium text-slate-700 disabled:opacity-50"
            >
              Next
            </button>
          </div>
        )}
      </div>

      {showForm && (
        <MemberFormModal
          member={editing}
          onClose={() => setShowForm(false)}
          onSaved={() => { setShowForm(false); load() }}
        />
      )}
    </div>
  )
}