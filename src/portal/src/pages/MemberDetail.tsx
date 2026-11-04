import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { apiClient } from '../api/client'
import type { Member, Membership, Plan, Routine, Trainer, PaymentRecord, Page } from '../types'
import { ArrowLeft, CreditCard, Dumbbell, User, Phone, FileText } from 'lucide-react'

export function MemberDetail() {
  const { id } = useParams()
  
  const [member, setMember] = useState<Member | null>(null)
  const [membership, setMembership] = useState<Membership | null>(null)
  const [plan, setPlan] = useState<Plan | null>(null)
  const [trainer, setTrainer] = useState<Trainer | null>(null)
  const [routine, setRoutine] = useState<Routine | null>(null)
  const [payments, setPayments] = useState<PaymentRecord[]>([])
  
  const [loading, setLoading] = useState(true)

  // Estados para el Modal de Pago
  const [showPaymentModal, setShowPaymentModal] = useState(false)
  const [amount, setAmount] = useState('')
  const [method, setMethod] = useState<'CASH' | 'TRANSFER' | 'CARD'>('CASH')
  const [saving, setSaving] = useState(false)

  async function loadData() {
    setLoading(true)
    try {
      const memRes = await apiClient.get<Member>(`/members/${id}`)
      setMember(memRes.data)

      const memshipsRes = await apiClient.get<Page<Membership>>('/memberships', { params: { memberId: id, status: 'ACTIVE' } })
      
      // Tomamos la más reciente por si hay varias activas (por el cambio de plan)
      const sortedMemships = memshipsRes.data.content.sort((a, b) => b.id - a.id)
      const activeMemship = sortedMemships.length > 0 ? sortedMemships[0] : null
      
      if (activeMemship) {
        setMembership(activeMemship)
        
        const planRes = await apiClient.get<Plan>(`/plans/${activeMemship.planId}`)
        setPlan(planRes.data)

        const payRes = await apiClient.get<PaymentRecord[]>(`/memberships/${activeMemship.id}/payments`)
        // Ordenamos los pagos del más reciente al más antiguo
        setPayments(payRes.data.sort((a, b) => new Date(b.recordedDate).getTime() - new Date(a.recordedDate).getTime()))
      } else {
        setMembership(null)
        setPlan(null)
        setPayments([])
      }

      if (memRes.data.trainerId) {
        const trainersRes = await apiClient.get<Trainer[]>('/trainers')
        const t = trainersRes.data.find(x => x.id === memRes.data.trainerId)
        if (t) setTrainer(t)
      }

      try {
        const routineRes = await apiClient.get<Routine>(`/routines/members/${id}`)
        setRoutine(routineRes.data)
      } catch (e) {
        setRoutine(null)
      }

    } catch (err) {
      console.error(err)
      alert('Failed to load member details')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [id])

  function openPaymentModal() {
    if (!membership) return alert('This member does not have an active membership')
    setAmount(membership.lockedPrice.toString())
    setMethod('CASH')
    setShowPaymentModal(true)
  }

  async function handlePayment(e: React.FormEvent) {
    e.preventDefault()
    if (!membership) return
    setSaving(true)
    try {
      await apiClient.post(`/memberships/${membership.id}/payments`, {
        amount: Number(amount),
        method,
        recordedDate: new Date().toISOString().split('T')[0]
      })
      setShowPaymentModal(false)
      loadData() // Recargamos para ver el nuevo pago en el historial
    } catch (err: any) {
      alert(err.response?.data?.detail ?? 'Failed to record payment')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className="p-8 text-center text-slate-500">Loading member profile...</div>
  if (!member) return <div className="p-8 text-center text-red-500">Member not found</div>

  return (
    <div className="max-w-5xl mx-auto">
      <div className="mb-6 flex justify-between items-start">
        <div>
          <Link to="/members" className="text-sm text-blue-600 hover:underline flex items-center gap-1 mb-4">
            <ArrowLeft size={16} /> Back to Members
          </Link>
          <p className="text-xs font-bold text-blue-600 uppercase tracking-wider mb-1">
            Member • {member.documentNumber}
          </p>
          <h1 className="text-3xl font-bold text-slate-800">{member.fullName}</h1>
        </div>
        
        {membership && (
          <button 
            onClick={openPaymentModal}
            className="flex items-center gap-2 bg-blue-700 hover:bg-blue-800 text-white px-4 py-2 rounded-lg font-medium shadow-sm transition-colors"
          >
            <CreditCard size={18} /> Record Payment
          </button>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Columna Izquierda: Perfil */}
        <div className="md:col-span-1">
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 flex flex-col items-center text-center">
            <div className="w-24 h-24 rounded-full bg-slate-100 flex items-center justify-center text-slate-500 text-3xl font-bold mb-4">
              {member.fullName.substring(0, 2).toUpperCase()}
            </div>
            <h2 className="text-xl font-bold text-slate-800">{member.fullName}</h2>
            <span className={`mt-2 px-3 py-1 rounded-full text-xs font-semibold ${member.active ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
              {member.active ? 'Active' : 'Inactive'}
            </span>

            <div className="w-full mt-6 space-y-4 text-left border-t border-slate-100 pt-6">
              <div>
                <p className="text-xs text-slate-400 mb-1 flex items-center gap-1"><Phone size={12}/> Contact</p>
                <p className="text-sm font-medium text-slate-700">{member.contact}</p>
              </div>
              <div>
                <p className="text-xs text-slate-400 mb-1 flex items-center gap-1"><FileText size={12}/> Document</p>
                <p className="text-sm font-medium text-slate-700">{member.documentNumber}</p>
              </div>
              <div>
                <p className="text-xs text-slate-400 mb-1 flex items-center gap-1"><User size={12}/> Assigned Trainer</p>
                <p className="text-sm font-medium text-slate-700">{trainer ? trainer.fullName : 'None'}</p>
              </div>
              <div>
                <p className="text-xs text-slate-400 mb-1">Access Code</p>
                <p className="text-sm font-bold text-blue-600 font-mono">{member.accessCode || 'N/A'}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Columna Derecha: Membresía, Rutina, Pagos */}
        <div className="md:col-span-2 space-y-6">
          {/* Active Membership */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
            <div className="flex justify-between items-center mb-4">
              <h3 className="font-bold text-slate-800 text-lg">Active Membership</h3>
              {membership && (
                <span className="px-3 py-1 rounded-full text-xs font-semibold bg-green-100 text-green-700">ACTIVE</span>
              )}
            </div>
            {membership ? (
              <div className="grid grid-cols-3 gap-4">
                <div>
                  <p className="text-xs text-slate-400 mb-1">Plan</p>
                  <p className="text-sm font-semibold text-slate-800">{plan?.name}</p>
                </div>
                <div>
                  <p className="text-xs text-slate-400 mb-1">Locked price</p>
                  <p className="text-sm font-semibold text-slate-800">${membership.lockedPrice.toFixed(2)}</p>
                </div>
                <div>
                  <p className="text-xs text-slate-400 mb-1">Expires</p>
                  <p className="text-sm font-semibold text-slate-800">{new Date(membership.endDate).toLocaleDateString()}</p>
                </div>
              </div>
            ) : (
              <p className="text-sm text-slate-500">No active membership.</p>
            )}
          </div>

          {/* Assigned Routine */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
            <h3 className="font-bold text-slate-800 text-lg mb-4">Assigned Routine</h3>
            {routine ? (
              <table className="w-full">
                <thead className="border-b border-slate-100">
                  <tr>
                    <th className="text-left pb-2 text-xs font-semibold text-slate-400 uppercase">Routine</th>
                    <th className="text-left pb-2 text-xs font-semibold text-slate-400 uppercase">Trainer</th>
                    <th className="text-right pb-2 text-xs font-semibold text-slate-400 uppercase">Status</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td className="py-3 text-sm font-medium text-slate-800 flex items-center gap-2">
                      <Dumbbell size={16} className="text-slate-400"/> {routine.name}
                    </td>
                    <td className="py-3 text-sm text-slate-600">{routine.trainerName}</td>
                    <td className="py-3 text-right">
                      <span className="px-2 py-1 rounded-full text-xs font-semibold bg-green-100 text-green-700">Active</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            ) : (
              <p className="text-sm text-slate-500">No routine assigned yet.</p>
            )}
          </div>

          {/* Payment History */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
            <h3 className="font-bold text-slate-800 text-lg mb-4">Payment history</h3>
            <table className="w-full">
              <thead className="border-b border-slate-100">
                <tr>
                  <th className="text-left pb-2 text-xs font-semibold text-slate-400 uppercase">Plan</th>
                  <th className="text-left pb-2 text-xs font-semibold text-slate-400 uppercase">Amount</th>
                  <th className="text-left pb-2 text-xs font-semibold text-slate-400 uppercase">Method</th>
                  <th className="text-right pb-2 text-xs font-semibold text-slate-400 uppercase">Date</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50">
                {payments.length === 0 && (
                  <tr><td colSpan={4} className="py-4 text-center text-sm text-slate-500">No payments recorded</td></tr>
                )}
                {payments.map(p => (
                  <tr key={p.id}>
                    <td className="py-3 text-sm text-slate-800">{plan?.name}</td>
                    <td className="py-3 text-sm font-medium text-slate-800">${p.amount.toFixed(2)}</td>
                    <td className="py-3 text-sm text-slate-600">
                      <span className="px-2 py-1 bg-slate-100 rounded text-xs">{p.method}</span>
                    </td>
                    <td className="py-3 text-right text-sm text-slate-500">{p.recordedDate}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Modal para Registrar Pago */}
      {showPaymentModal && membership && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-full max-w-sm shadow-xl">
            <div className="flex items-center gap-2 mb-4 text-blue-600">
              <CreditCard size={24} />
              <h2 className="text-lg font-bold">Record Payment</h2>
            </div>
            <div className="mb-4 text-sm text-slate-600 bg-slate-50 p-3 rounded-lg">
              <p><strong>Member:</strong> {member.fullName}</p>
              <p><strong>Plan:</strong> {plan?.name}</p>
            </div>
            <form onSubmit={handlePayment} className="space-y-3">
              <label className="block">
                <span className="text-sm text-slate-700 mb-1">Amount</span>
                <input type="number" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} required className="w-full border border-slate-300 rounded px-3 py-2 focus:ring-2 focus:ring-blue-500 outline-none" />
              </label>
              <label className="block">
                <span className="text-sm text-slate-700 mb-1">Method</span>
                <select value={method} onChange={(e) => setMethod(e.target.value as any)} className="w-full border border-slate-300 rounded px-3 py-2 focus:ring-2 focus:ring-blue-500 outline-none">
                  <option value="CASH">Cash</option>
                  <option value="TRANSFER">Transfer</option>
                  <option value="CARD">Card</option>
                </select>
              </label>
              <div className="flex justify-end gap-2 pt-2">
                <button type="button" onClick={() => setShowPaymentModal(false)} className="px-4 py-2 bg-slate-200 hover:bg-slate-300 rounded text-slate-700">Cancel</button>
                <button type="submit" disabled={saving} className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded disabled:opacity-50">
                  {saving ? 'Saving...' : 'Save Payment'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}