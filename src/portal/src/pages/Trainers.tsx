
import { useEffect, useState } from 'react'
import { apiClient } from '../api/client'
import type { Trainer, Member, Plan, Membership } from '../types'
import { UserPlus, Users, Search } from 'lucide-react'
import { AxiosError } from 'axios'

export function Trainers() {
  const [trainers, setTrainers] = useState<Trainer[]>([])
  const [members, setMembers] = useState<Member[]>([])
  const [plans, setPlans] = useState<Plan[]>([])
  const [memberships, setMemberships] = useState<Membership[]>([])

  const [selectedTrainer, setSelectedTrainer] = useState<Trainer | null>(null)
  const [trainerMembers, setTrainerMembers] = useState<Member[]>([])

  // Modal state
  const [showAssignModal, setShowAssignModal] = useState(false)
  const [selectedMemberId, setSelectedMemberId] = useState('')
  const [assignTrainerId, setAssignTrainerId] = useState('')

  // Search state
  const [searchDoc, setSearchDoc] = useState('')

  async function load() {
    try {
      const [tRes, mRes, plansRes, memshipsRes] = await Promise.all([
        apiClient.get<Trainer[]>('/trainers'),

        apiClient.get<{ content: Member[] }>('/members', {
          params: { page: 0, size: 100 },
        }),

        apiClient.get<{ content: Plan[] }>('/plans', {
          params: { page: 0, size: 100 },
        }),

        apiClient.get<{ content: Membership[] }>('/memberships', {
          params: { page: 0, size: 1000, status: 'ACTIVE' },
        }),
      ])

      setTrainers(tRes.data)
      setMembers(mRes.data.content)
      setPlans(plansRes.data.content)
      setMemberships(memshipsRes.data.content)
    } catch (err) {
      console.error('Error loading trainers data:', err)
    }
  }

  useEffect(() => {
    load()
  }, [])

  // Cuando se selecciona un trainer, filtramos los miembros asignados.
  useEffect(() => {
    if (selectedTrainer) {
      const assigned = members.filter(
        m => m.trainerId === selectedTrainer.id
      )

      setTrainerMembers(assigned)
    } else {
      setTrainerMembers([])
    }
  }, [selectedTrainer, members])

  // Obtener el nombre del plan activo de un miembro.
  function getPlanName(memberId: number) {
    const membership = memberships.find(
      m => m.memberId === memberId && m.status === 'ACTIVE'
    )

    if (!membership) return '—'

    const plan = plans.find(p => p.id === membership.planId)

    return plan ? plan.name : '—'
  }

  async function handleAssign(e: React.FormEvent) {
    e.preventDefault()

    if (!assignTrainerId || !selectedMemberId) return

    try {
      await apiClient.patch(
        `/members/${selectedMemberId}/trainer`,
        { trainerId: Number(assignTrainerId) }
      )

      setShowAssignModal(false)
      setSelectedMemberId('')
      setAssignTrainerId('')
      setSearchDoc('')

      alert('Trainer assigned successfully')

      // Recargar todo; el useEffect actualiza la vista del trainer seleccionado.
      await load()
    } catch (err) {
      const axiosErr = err as AxiosError<{ detail?: string }>

      console.error(axiosErr)

      alert(
        axiosErr.response?.data?.detail ?? 'Failed to assign trainer'
      )
    }
  }

  // Filtro de búsqueda para el modal.
  const filteredMembers = members.filter(m =>
    m.documentNumber.includes(searchDoc) ||
    m.fullName.toLowerCase().includes(searchDoc.toLowerCase())
  )

  return (
    <div className="flex gap-6 h-[calc(100vh-8rem)]">

      {/* Lista de Entrenadores */}
      <div className="w-1/3 flex flex-col">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-bold text-slate-800">
            Trainers
          </h2>

          <button
            onClick={() => setShowAssignModal(true)}
            className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-3 py-2 rounded-lg text-sm shadow-sm"
          >
            <UserPlus size={16} />
            Assign
          </button>
        </div>

        <div className="space-y-2 overflow-y-auto pr-2">
          {trainers.length === 0 && (
            <p className="text-sm text-slate-500">
              No trainers yet.
            </p>
          )}

          {trainers.map(t => (
            <div
              key={t.id}
              onClick={() => setSelectedTrainer(t)}
              className={`p-4 rounded-xl cursor-pointer border transition-colors ${
                selectedTrainer?.id === t.id
                  ? 'bg-blue-50 border-blue-300'
                  : 'bg-white border-slate-200 hover:border-blue-300 shadow-sm'
              }`}
            >
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-slate-100 flex items-center justify-center text-slate-600 font-bold">
                  {t.fullName.substring(0, 2).toUpperCase()}
                </div>

                <div>
                  <p className="font-semibold text-slate-800">
                    {t.fullName}
                  </p>

                  <p className="text-xs text-slate-500">
                    {members.filter(m => m.trainerId === t.id).length} assigned members
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Detalle del Entrenador y sus Clientes */}
      <div className="flex-1">
        {selectedTrainer ? (
          <div className="bg-white rounded-xl shadow-sm p-6 border border-slate-200 h-full">

            <div className="flex items-center gap-4 mb-8 pb-6 border-b border-slate-200">
              <div className="w-16 h-16 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 text-xl font-bold">
                {selectedTrainer.fullName.substring(0, 2).toUpperCase()}
              </div>

              <div>
                <h2 className="text-2xl font-bold text-slate-800">
                  {selectedTrainer.fullName}
                </h2>

                <p className="text-slate-500">
                  Trainer Profile
                </p>
              </div>
            </div>

            <div className="flex justify-between items-center mb-4">
              <h3 className="font-semibold text-slate-700 flex items-center gap-2">
                <Users size={18} />
                Assigned Members ({trainerMembers.length})
              </h3>
            </div>

            <table className="w-full">
              <thead className="bg-slate-50 border-y border-slate-200">
                <tr>
                  <th className="px-4 py-2 text-left text-xs font-semibold text-slate-500">
                    Document
                  </th>

                  <th className="px-4 py-2 text-left text-xs font-semibold text-slate-500">
                    Name
                  </th>

                  <th className="px-4 py-2 text-left text-xs font-semibold text-slate-500">
                    Contact
                  </th>

                  <th className="px-4 py-2 text-left text-xs font-semibold text-slate-500">
                    Plan
                  </th>
                </tr>
              </thead>

              <tbody className="divide-y divide-slate-100">
                {trainerMembers.length === 0 && (
                  <tr>
                    <td
                      colSpan={4}
                      className="text-center py-8 text-slate-400 text-sm"
                    >
                      This trainer has no assigned members yet.
                    </td>
                  </tr>
                )}

                {trainerMembers.map(m => (
                  <tr key={m.id} className="hover:bg-slate-50">
                    <td className="px-4 py-3 font-medium text-slate-800">
                      {m.documentNumber}
                    </td>

                    <td className="px-4 py-3 text-slate-600">
                      {m.fullName}
                    </td>

                    <td className="px-4 py-3 text-slate-600">
                      {m.contact}
                    </td>

                    <td className="px-4 py-3 text-slate-600">
                      {getPlanName(m.id)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="bg-white rounded-xl shadow-sm p-12 text-center text-slate-400 flex flex-col items-center border border-slate-200 h-full justify-center">
            <Users size={48} className="mb-4 text-slate-300" />
            <p>Select a trainer to view their assigned members</p>
          </div>
        )}
      </div>

      {/* Modal Asignar */}
      {showAssignModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-full max-w-md shadow-xl">

            <h2 className="text-lg font-bold mb-4">
              Assign Trainer to Member
            </h2>

            <form onSubmit={handleAssign} className="space-y-4">

              <label className="block">
                <span className="text-sm text-slate-700 mb-1">
                  1. Select Trainer
                </span>

                <select
                  value={assignTrainerId}
                  onChange={e => setAssignTrainerId(e.target.value)}
                  required
                  className="w-full border border-slate-300 rounded px-3 py-2 focus:ring-2 focus:ring-blue-500 outline-none"
                >
                  <option value="">Choose a trainer...</option>

                  {trainers.map(t => (
                    <option key={t.id} value={t.id}>
                      {t.fullName}
                    </option>
                  ))}
                </select>
              </label>

              <div className="border-t border-slate-200 pt-4">
                <label className="block mb-2">
                  <span className="text-sm text-slate-700 mb-1">
                    2. Search Member by Document or Name
                  </span>

                  <div className="relative">
                    <Search
                      className="absolute left-3 top-2.5 text-slate-400"
                      size={16}
                    />

                    <input
                      type="text"
                      placeholder="Search..."
                      value={searchDoc}
                      onChange={e => setSearchDoc(e.target.value)}
                      className="w-full border border-slate-300 rounded pl-9 pr-3 py-2 focus:ring-2 focus:ring-blue-500 outline-none"
                    />
                  </div>
                </label>

                <label className="block">
                  <span className="text-sm text-slate-700 mb-1">
                    3. Select Member
                  </span>

                  <select
                    value={selectedMemberId}
                    onChange={e => setSelectedMemberId(e.target.value)}
                    required
                    className="w-full border border-slate-300 rounded px-3 py-2 focus:ring-2 focus:ring-blue-500 outline-none"
                  >
                    <option value="">Choose a member...</option>

                    {filteredMembers.map(m => (
                      <option key={m.id} value={m.id}>
                        {m.documentNumber} - {m.fullName} {m.trainerId ? '(Has trainer)' : ''}
                      </option>
                    ))}
                  </select>
                </label>
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowAssignModal(false)}
                  className="px-4 py-2 bg-slate-200 hover:bg-slate-300 rounded text-slate-700"
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  disabled={!assignTrainerId || !selectedMemberId}
                  className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded disabled:opacity-50"
                >
                  Assign
                </button>
              </div>

            </form>
          </div>
        </div>
      )}
    </div>
  )
}
