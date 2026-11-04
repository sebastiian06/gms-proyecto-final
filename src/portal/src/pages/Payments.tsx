import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { apiClient } from '../api/client'
import type { Membership, Member, Plan, Page, PaymentRecord } from '../types'
import { AxiosError } from 'axios'
import { Search, Plus, CreditCard } from 'lucide-react'
import { useAuth } from '../auth/AuthContext'

interface PaymentRow {
  payment: PaymentRecord
  membership: Membership
  member: Member
  plan: Plan
}

export function Payments() {
  const { user } = useAuth()
  const isAdmin = user?.role === 'ADMIN'

  const [rows, setRows] = useState<PaymentRow[]>([])
  const [allMemberships, setAllMemberships] = useState<Membership[]>([])
  const [allMembers, setAllMembers] = useState<Member[]>([])
  
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [search, setSearch] = useState('')

  // Modal State
  const [showModal, setShowModal] = useState(false)
  const [editingPayment, setEditingPayment] = useState<PaymentRecord | null>(null)
  
  // Estado para nuevo pago
  const [selectedMemberId, setSelectedMemberId] = useState('')
  const [amount, setAmount] = useState('')
  const [method, setMethod] = useState<'CASH' | 'TRANSFER' | 'CARD'>('CASH')
  const [saving, setSaving] = useState(false)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      const [memshipsRes, membersRes, plansRes] = await Promise.all([
        apiClient.get<Page<Membership>>('/memberships', { params: { page: 0, size: 1000 } }),
        apiClient.get<Page<Member>>('/members', { params: { page: 0, size: 1000 } }),
        apiClient.get<Page<Plan>>('/plans', { params: { page: 0, size: 100 } })
      ])
      
      const memberships = memshipsRes.data.content
      const members = membersRes.data.content
      const plansData = plansRes.data.content

      setAllMemberships(memberships)
      setAllMembers(members)

      const allRows: PaymentRow[] = []
      
      await Promise.all(memberships.map(async (m) => {
        try {
          const payRes = await apiClient.get<PaymentRecord[]>(`/memberships/${m.id}/payments`)
          const member = members.find(x => x.id === m.memberId)
          const plan = plansData.find(x => x.id === m.planId)
          
          if (member && plan) {
            payRes.data.forEach(p => {
              allRows.push({ payment: p, membership: m, member, plan })
            })
          }
        } catch (e) { /* Ignorar */ }
      }))

      allRows.sort((a, b) => new Date(b.payment.recordedDate).getTime() - new Date(a.payment.recordedDate).getTime())
      setRows(allRows)

    } catch (err) {
      const axiosErr = err as AxiosError<{ detail?: string }>
      setError(axiosErr.response?.data?.detail ?? 'Failed to load payments')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  function openEditModal(row: PaymentRow) {
    setEditingPayment(row.payment)
    setSelectedMemberId(row.member.id.toString())
    setAmount(row.payment.amount.toString())
    setMethod(row.payment.method as any)
    setShowModal(true)
  }

  function openNewModal() {
    setEditingPayment(null)
    setSelectedMemberId('')
    setAmount('')
    setMethod('CASH')
    setShowModal(true)
  }

  async function handleSave(e: React.FormEvent) {
    e.preventDefault()
    setSaving(true)
    try {
      if (editingPayment) {
        await apiClient.put(`/payments/${editingPayment.id}`, { amount: Number(amount), method })
      } else {
        // Si es nuevo, buscamos la membresía activa del miembro seleccionado
        const memship = allMemberships.find(m => m.memberId === Number(selectedMemberId) && m.status === 'ACTIVE')
        if (!memship) {
          alert('This member does not have an active membership.')
          setSaving(false)
          return
        }
        
        await apiClient.post(`/memberships/${memship.id}/payments`, {
          amount: Number(amount),
          method,
          recordedDate: new Date().toISOString().split('T')[0]
        })
      }
      setShowModal(false)
      load()
    } catch (err: any) {
      alert(err.response?.data?.detail ?? 'Failed to save')
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(id: number) {
    if (!confirm('Delete this payment?')) return
    try {
      await apiClient.delete(`/payments/${id}`)
      load()
    } catch (err) {
      alert('Failed to delete. Check backend.')
    }
  }

  const filteredRows = rows.filter(r => 
    r.member.fullName.toLowerCase().includes(search.toLowerCase()) ||
    r.member.documentNumber.includes(search)
  )

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-slate-800">Payment History</h1>
        <button
          onClick={openNewModal}
          className="flex items-center gap-2 bg-blue-700 hover:bg-blue-800 text-white px-5 py-2.5 rounded-lg font-medium shadow-sm transition-colors"
        >
          <Plus size={18} /> Record Payment
        </button>
      </div>

      {error && <div className="bg-red-100 text-red-700 p-3 rounded-lg mb-4">{error}</div>}

      <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
        <div className="p-4 border-b border-slate-200 bg-slate-50/50">
          <div className="relative max-w-md">
            <Search className="absolute left-3 top-2.5 text-slate-400" size={16} />
            <input
              type="text"
              placeholder="Search by member name or document..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full border border-slate-300 rounded-lg pl-9 pr-3 py-2 text-sm focus:ring-2 focus:ring-blue-500 outline-none"
            />
          </div>
        </div>

        <table className="w-full">
          <thead className="bg-white border-b border-slate-200">
            <tr>
              <th className="px-6 py-4 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">Member</th>
              <th className="px-6 py-4 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">Document</th>
              <th className="px-6 py-4 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">Plan</th>
              <th className="px-6 py-4 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">Amount</th>
              <th className="px-6 py-4 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">Method</th>
              <th className="px-6 py-4 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">Date</th>
              <th className="px-6 py-4 text-right text-xs font-semibold text-slate-500 uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {loading && <tr><td colSpan={7} className="text-center py-8 text-slate-500">Loading...</td></tr>}
            {!loading && filteredRows.length === 0 && (
              <tr><td colSpan={7} className="text-center py-8 text-slate-500">No payments found</td></tr>
            )}
            {filteredRows.map((r) => (
              <tr key={r.payment.id} className="hover:bg-slate-50">
                <td className="px-6 py-4 text-sm font-medium text-slate-800">{r.member.fullName}</td>
                <td className="px-6 py-4 text-sm text-slate-600">{r.member.documentNumber}</td>
                <td className="px-6 py-4 text-sm text-slate-600">{r.plan.name}</td>
                <td className="px-6 py-4 text-sm font-semibold">${r.payment.amount.toFixed(2)}</td>
                <td className="px-6 py-4 text-sm">
                  <span className="px-2 py-1 bg-slate-100 rounded text-xs font-medium">{r.payment.method}</span>
                </td>
                <td className="px-6 py-4 text-sm text-slate-600">{r.payment.recordedDate}</td>
                <td className="px-6 py-4 text-right space-x-2">
                  <Link to={`/members/${r.member.id}`} className="inline-flex items-center justify-center px-4 py-1.5 border border-slate-300 rounded-lg text-sm font-medium text-slate-700 bg-white hover:bg-slate-50 transition-colors">
                    View
                  </Link>
                  {isAdmin && (
                    <>
                      <button onClick={() => openEditModal(r)} className="inline-flex items-center justify-center px-4 py-1.5 border border-slate-300 rounded-lg text-sm font-medium text-slate-700 bg-white hover:bg-slate-50 transition-colors">
                        Edit
                      </button>
                      <button onClick={() => handleDelete(r.payment.id)} className="inline-flex items-center justify-center px-4 py-1.5 rounded-lg text-sm font-medium text-white bg-red-600 hover:bg-red-700 transition-colors shadow-sm">
                        Delete
                      </button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-full max-w-sm shadow-xl">
            <div className="flex items-center gap-2 mb-4 text-blue-600">
              <CreditCard size={24} />
              <h2 className="text-lg font-bold">{editingPayment ? 'Edit Payment' : 'Record Payment'}</h2>
            </div>
            
            <form onSubmit={handleSave} className="space-y-3">
              {!editingPayment && (
                <label className="block">
                  <span className="text-sm text-slate-700 mb-1">Member</span>
                  <select 
                    value={selectedMemberId} 
                    onChange={(e) => setSelectedMemberId(e.target.value)} 
                    required 
                    className="w-full border border-slate-300 rounded px-3 py-2"
                  >
                    <option value="">Select a member...</option>
                    {allMembers.map(m => (
                      <option key={m.id} value={m.id}>{m.fullName} ({m.documentNumber})</option>
                    ))}
                  </select>
                </label>
              )}

              <label className="block">
                <span className="text-sm text-slate-700 mb-1">Amount</span>
                <input type="number" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} required className="w-full border border-slate-300 rounded px-3 py-2" />
              </label>
              
              <label className="block">
                <span className="text-sm text-slate-700 mb-1">Method</span>
                <select value={method} onChange={(e) => setMethod(e.target.value as any)} className="w-full border border-slate-300 rounded px-3 py-2">
                  <option value="CASH">Cash</option>
                  <option value="TRANSFER">Transfer</option>
                  <option value="CARD">Card</option>
                </select>
              </label>
              
              <div className="flex justify-end gap-2 pt-2">
                <button type="button" onClick={() => setShowModal(false)} className="px-4 py-2 bg-slate-200 rounded">Cancel</button>
                <button type="submit" disabled={saving} className="px-4 py-2 bg-blue-600 text-white rounded">{saving ? 'Saving...' : 'Save'}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}