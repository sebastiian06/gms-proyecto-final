import { useEffect, useState } from 'react'
import { apiClient } from '../api/client'
import type { Member, Membership, Plan, Routine, PaymentRecord, Page } from '../types'
import { useAuth } from '../auth/AuthContext'
import { Dumbbell, User, Phone, FileText } from 'lucide-react'

export function MyProfile() {
  const { user } = useAuth()
  
  const [member, setMember] = useState<Member | null>(null)
  const [membership, setMembership] = useState<Membership | null>(null)
  const [plan, setPlan] = useState<Plan | null>(null)
  const [routine, setRoutine] = useState<Routine | null>(null)
  const [payments, setPayments] = useState<PaymentRecord[]>([])
  
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function load() {
      try {
        // 1. Traemos el perfil del miembro autenticado
        const myMemberRes = await apiClient.get<Member>('/members/me')
        const myMember = myMemberRes.data
        setMember(myMember)

        // 2. Buscamos la membresía activa más reciente
        const memshipsRes = await apiClient.get<Page<Membership>>('/memberships', { params: { memberId: myMember.id, status: 'ACTIVE' } })
        const sortedMemships = memshipsRes.data.content.sort((a, b) => b.id - a.id)
        const activeMemship = sortedMemships.length > 0 ? sortedMemships[0] : null
        
        if (activeMemship) {
          setMembership(activeMemship)
          
          // 3. Traemos el plan
          const planRes = await apiClient.get<Plan>(`/plans/${activeMemship.planId}`)
          setPlan(planRes.data)

          // 4. Traemos los pagos
          const payRes = await apiClient.get<PaymentRecord[]>(`/memberships/${activeMemship.id}/payments`)
          setPayments(payRes.data.sort((a, b) => new Date(b.recordedDate).getTime() - new Date(a.recordedDate).getTime()))
        }

        // 5. Traemos la rutina
        console.log("Member ID para rutina:", myMember.id)
        try {
          const routineRes = await apiClient.get<Routine>(`/routines/members/${myMember.id}`)
          console.log("Rutina obtenida:", routineRes.data)
          setRoutine(routineRes.data)
        } catch (e) {
            console.log("Error al obtener rutina:", e)
        }

      } catch (err) {
        console.error(err)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [user])

  if (loading) return <div className="p-8 text-center text-slate-500">Loading your profile...</div>
  if (!member) return <div className="p-8 text-center text-slate-500">No member profile found for this account.</div>

  return (
    <div className="max-w-5xl mx-auto">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-slate-800">My Profile</h1>
        <p className="text-slate-500">View your membership details, routine and payment history.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Columna Izquierda: Perfil */}
        <div className="md:col-span-1">
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 flex flex-col items-center text-center">
            <div className="w-24 h-24 rounded-full bg-blue-50 flex items-center justify-center text-blue-600 text-3xl font-bold mb-4">
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
                <p className="text-xs text-slate-400 mb-1 flex items-center gap-1"><User size={12}/> My Trainer</p>
                <p className="text-sm font-medium text-slate-700">{member.trainerName || 'Not assigned'}</p>
              </div>
              <div className="bg-blue-50 p-3 rounded-lg border border-blue-100">
                <p className="text-xs text-blue-500 mb-1 font-semibold">My Access Code</p>
                <p className="text-2xl font-bold text-blue-700 font-mono tracking-widest">{member.accessCode || 'N/A'}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Columna Derecha */}
        <div className="md:col-span-2 space-y-6">
          {/* Membresía */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
            <div className="flex justify-between items-center mb-4">
              <h3 className="font-bold text-slate-800 text-lg">My Membership</h3>
              {membership && <span className="px-3 py-1 rounded-full text-xs font-semibold bg-green-100 text-green-700">ACTIVE</span>}
            </div>
            {membership ? (
              <div className="grid grid-cols-3 gap-4">
                <div>
                  <p className="text-xs text-slate-400 mb-1">Plan</p>
                  <p className="text-sm font-semibold text-slate-800">{plan?.name}</p>
                </div>
                <div>
                  <p className="text-xs text-slate-400 mb-1">Price</p>
                  <p className="text-sm font-semibold text-slate-800">${membership.lockedPrice.toFixed(2)}</p>
                </div>
                <div>
                  <p className="text-xs text-slate-400 mb-1">Expires</p>
                  <p className="text-sm font-semibold text-slate-800">{new Date(membership.endDate).toLocaleDateString()}</p>
                </div>
              </div>
            ) : (
              <p className="text-sm text-slate-500">You don't have an active membership.</p>
            )}
          </div>

          {/* Rutina */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
            <h3 className="font-bold text-slate-800 text-lg mb-4 flex items-center gap-2"><Dumbbell size={18}/> My Routine</h3>
            {routine ? (
              <div className="bg-slate-50 rounded-xl p-4 border border-slate-100">
                <p className="font-semibold text-lg text-slate-800 mb-1">{routine.name}</p>
                <p className="text-sm text-slate-500">Assigned by {routine.trainerName}</p>
              </div>
            ) : (
              <p className="text-sm text-slate-500">No routine assigned yet. Ask your trainer.</p>
            )}
          </div>

          {/* Historial Pagos */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
            <h3 className="font-bold text-slate-800 text-lg mb-4">My Payment History</h3>
            <table className="w-full">
              <thead className="border-b border-slate-100">
                <tr>
                  <th className="text-left pb-2 text-xs font-semibold text-slate-400 uppercase">Plan</th>
                  <th className="text-left pb-2 text-xs font-semibold text-slate-400 uppercase">Amount</th>
                  <th className="text-right pb-2 text-xs font-semibold text-slate-400 uppercase">Date</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50">
                {payments.length === 0 && <tr><td colSpan={3} className="py-4 text-center text-sm text-slate-500">No payments</td></tr>}
                {payments.map(p => (
                  <tr key={p.id}>
                    <td className="py-3 text-sm text-slate-800">{plan?.name}</td>
                    <td className="py-3 text-sm font-medium text-slate-800">${p.amount.toFixed(2)}</td>
                    <td className="py-3 text-right text-sm text-slate-500">{p.recordedDate}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  )
}