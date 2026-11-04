
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { apiClient } from '../api/client'
import type { Member, Trainer, Page, Membership, Plan, Routine } from '../types'
import { useAuth } from '../auth/AuthContext'
import { Search } from 'lucide-react'

export function AssignedMembers() {
  const { user } = useAuth()

  const [members, setMembers] = useState<Member[]>([])
  const [memberships, setMemberships] = useState<Membership[]>([])
  const [plans, setPlans] = useState<Plan[]>([])
  const [routines, setRoutines] = useState<Record<number, string>>({})

  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('')

  useEffect(() => {
    async function load() {
      try {
        const tRes = await apiClient.get<Trainer[]>('/trainers')

        const currentTrainer = tRes.data.find(
          (t) => t.userId === user?.id,
        )

        if (!currentTrainer) {
          setMembers([])
          setMemberships([])
          setPlans([])
          setRoutines({})
          return
        }

        const [mRes, memshipsRes, plansRes] = await Promise.all([
          apiClient.get<Page<Member>>('/members', {
            params: { page: 0, size: 100 },
          }),

          apiClient.get<Page<Membership>>('/memberships', {
            params: {
              page: 0,
              size: 1000,
              status: 'ACTIVE',
            },
          }),

          apiClient.get<Page<Plan>>('/plans', {
            params: { page: 0, size: 100 },
          }),
        ])

        const assignedMembers = mRes.data.content.filter(
          (m) => m.trainerId === currentTrainer.id,
        )

        setMembers(assignedMembers)
        setMemberships(memshipsRes.data.content)
        setPlans(plansRes.data.content)

        // Consultar la rutina de cada miembro
        const routineResults = await Promise.all(
          assignedMembers.map(async (member) => {
            try {
              const response = await apiClient.get<Routine>(
                `/routines/members/${member.id}`,
              )

              return {
                memberId: member.id,
                routineName: response.data.name,
              }
            } catch {
              return {
                memberId: member.id,
                routineName: 'No routine',
              }
            }
          }),
        )

        // Asociar el nombre de la rutina con el ID del miembro
        const routineMap: Record<number, string> = {}

        routineResults.forEach((result) => {
          routineMap[result.memberId] = result.routineName
        })

        setRoutines(routineMap)

      } catch (err) {
        console.error('Error loading assigned members:', err)
      }
    }

    load()
  }, [user])

  function getPlanName(memberId: number) {
    const memship = memberships.find(
      (m) => m.memberId === memberId,
    )

    if (!memship) return '—'

    const plan = plans.find((p) => p.id === memship.planId)

    return plan ? plan.name : '—'
  }

  function getRoutineName(memberId: number) {
    return routines[memberId] ?? 'No routine'
  }

  const filteredMembers = members.filter((m) => {
    const matchSearch =
      m.fullName.toLowerCase().includes(search.toLowerCase()) ||
      m.documentNumber.includes(search)

    const matchStatus =
      statusFilter === ''
        ? true
        : statusFilter === 'active'
          ? m.active
          : !m.active

    return matchSearch && matchStatus
  })

  return (
    <div>
      <h1 className="text-2xl font-bold text-slate-800 mb-6">
        My Clients
      </h1>

      <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
        <div className="p-4 border-b border-slate-200 flex gap-4 bg-slate-50/50">
          <div className="flex-1">
            <label className="block text-xs font-semibold text-slate-600 mb-1">
              Search members
            </label>

            <div className="relative">
              <Search
                className="absolute left-3 top-2.5 text-slate-400"
                size={16}
              />

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
            <label className="block text-xs font-semibold text-slate-600 mb-1">
              Status
            </label>

            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500 outline-none"
            >
              <option value="">All</option>
              <option value="active">Active</option>
              <option value="inactive">Inactive</option>
            </select>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-white border-b border-slate-200">
              <tr>
                <th className="px-6 py-4 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Document
                </th>

                <th className="px-6 py-4 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Full name
                </th>

                <th className="px-6 py-4 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Contact
                </th>

                <th className="px-6 py-4 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Plan
                </th>

                <th className="px-6 py-4 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Routine
                </th>

                <th className="px-6 py-4 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Status
                </th>

                <th className="px-6 py-4 text-right text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>

            <tbody className="divide-y divide-slate-100">
              {filteredMembers.length === 0 && (
                <tr>
                  <td
                    colSpan={7}
                    className="text-center py-8 text-slate-500"
                  >
                    No clients found
                  </td>
                </tr>
              )}

              {filteredMembers.map((m) => (
                <tr key={m.id} className="hover:bg-slate-50">
                  <td className="px-6 py-4 text-sm text-slate-600">
                    {m.documentNumber}
                  </td>

                  <td className="px-6 py-4 text-sm font-medium text-slate-800">
                    {m.fullName}
                  </td>

                  <td className="px-6 py-4 text-sm text-slate-600">
                    {m.contact}
                  </td>

                  <td className="px-6 py-4 text-sm text-slate-600">
                    {getPlanName(m.id)}
                  </td>

                  <td className="px-6 py-4 text-sm text-slate-600">
                    {getRoutineName(m.id)}
                  </td>

                  <td className="px-6 py-4 text-sm">
                    <span
                      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        m.active
                          ? 'bg-green-50 text-green-700 border border-green-200'
                          : 'bg-red-50 text-red-700 border border-red-200'
                      }`}
                    >
                      {m.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>

                  <td className="px-6 py-4 text-right">
                    <Link
                      to={`/members/${m.id}`}
                      className="inline-flex items-center justify-center px-4 py-1.5 border border-slate-300 rounded-lg text-sm font-medium text-slate-700 bg-white hover:bg-slate-50 transition-colors"
                    >
                      View
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
