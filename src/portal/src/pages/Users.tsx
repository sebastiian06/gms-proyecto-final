import { useEffect, useState } from 'react'
import { apiClient } from '../api/client'
import type { User, Role } from '../types'
import { UserFormModal } from '../components/UserFormModal'
import { AxiosError } from 'axios'
import { Plus, UserCheck, UserX, Pencil } from 'lucide-react'

export function Users() {
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState<User | null>(null)
  const [roleFilter, setRoleFilter] = useState<Role | ''>('')
  const [activeFilter, setActiveFilter] = useState<'' | 'true' | 'false'>('')

  async function load() {
    setLoading(true)
    setError(null)
    try {
      const params: Record<string, string> = {}
      if (roleFilter) params.role = roleFilter
      if (activeFilter) params.active = activeFilter
      const { data } = await apiClient.get<User[]>('/users', { params })
      setUsers(data)
    } catch (err) {
      const axiosErr = err as AxiosError<{ detail?: string }>
      setError(axiosErr.response?.data?.detail ?? 'Failed to load users')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [roleFilter, activeFilter])

  async function handleDeactivate(id: number) {
    if (!confirm('Deactivate this user? They will not be able to log in.')) return
    try {
      await apiClient.patch(`/users/${id}/deactivate`)
      load()
    } catch (err) {
      const axiosErr = err as AxiosError<{ detail?: string }>
      alert(axiosErr.response?.data?.detail ?? 'Failed to deactivate')
    }
  }

  async function handleActivate(id: number) {
    try {
      await apiClient.patch(`/users/${id}/activate`)
      load()
    } catch (err) {
      const axiosErr = err as AxiosError<{ detail?: string }>
      alert(axiosErr.response?.data?.detail ?? 'Failed to activate')
    }
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-slate-800">Users</h1>
        <button
          onClick={() => { setEditing(null); setShowForm(true) }}
          className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg"
        >
          <Plus size={16} /> New user
        </button>
      </div>

      <div className="flex gap-3 mb-4">
        <select
          value={roleFilter}
          onChange={(e) => setRoleFilter(e.target.value as Role | '')}
          className="border border-slate-300 rounded-lg px-3 py-2 text-sm"
        >
          <option value="">All roles</option>
          <option value="ADMIN">Admin</option>
          <option value="RECEPTIONIST">Receptionist</option>
          <option value="TRAINER">Trainer</option>
          <option value="MEMBER">Member</option>
        </select>
        <select
          value={activeFilter}
          onChange={(e) => setActiveFilter(e.target.value as '' | 'true' | 'false')}
          className="border border-slate-300 rounded-lg px-3 py-2 text-sm"
        >
          <option value="">All statuses</option>
          <option value="true">Active only</option>
          <option value="false">Inactive only</option>
        </select>
      </div>

      {error && <div className="bg-red-100 text-red-700 p-3 rounded-lg mb-4">{error}</div>}
      {loading && <p className="text-slate-500">Loading...</p>}

      <div className="bg-white rounded-xl shadow-sm overflow-hidden">
        <table className="w-full">
          <thead className="bg-slate-50 border-b border-slate-200">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">Username</th>
              <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">Role</th>
              <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">Status</th>
              <th className="px-4 py-3 text-right text-xs font-semibold text-slate-500 uppercase">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200">
            {users.length === 0 && (
              <tr><td colSpan={4} className="px-4 py-8 text-center text-slate-500">No users yet</td></tr>
            )}
            {users.map((u) => (
              <tr key={u.id}>
                <td className="px-4 py-3 font-medium">
                  {u.username}
                  {u.isSystem && <span className="ml-2 text-xs px-2 py-0.5 bg-amber-100 text-amber-700 rounded">system</span>}
                </td>
                <td className="px-4 py-3">
                  <span className="text-xs px-2 py-1 bg-blue-100 text-blue-700 rounded-full">{u.role}</span>
                </td>
                <td className="px-4 py-3">
                  {u.active
                    ? <span className="text-green-600 text-sm font-medium">Active</span>
                    : <span className="text-red-600 text-sm font-medium">Inactive</span>}
                </td>
                <td className="px-4 py-3 text-right space-x-2">
                  <button
                    onClick={() => { setEditing(u); setShowForm(true) }}
                    className="text-blue-600 hover:underline text-sm inline-flex items-center gap-1"
                  >
                    <Pencil size={14} /> Edit
                  </button>
                  {!u.isSystem && (
                    u.active
                      ? <button onClick={() => handleDeactivate(u.id)} className="text-red-600 hover:underline text-sm inline-flex items-center gap-1"><UserX size={14} /> Deactivate</button>
                      : <button onClick={() => handleActivate(u.id)} className="text-green-600 hover:underline text-sm inline-flex items-center gap-1"><UserCheck size={14} /> Activate</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showForm && (
        <UserFormModal
          user={editing}
          onClose={() => setShowForm(false)}
          onSaved={() => { setShowForm(false); load() }}
        />
      )}
    </div>
  )
}