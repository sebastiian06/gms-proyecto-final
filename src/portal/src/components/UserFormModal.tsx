import { useState, type FormEvent } from 'react'
import { apiClient } from '../api/client'
import type { User, Role } from '../types'
import { AxiosError } from 'axios'

interface Props {
  user: User | null
  onClose: () => void
  onSaved: () => void
}

export function UserFormModal({ user, onClose, onSaved }: Props) {
  const isEdit = !!user
  const [username, setUsername] = useState(user?.username ?? '')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState<Role>(user?.role ?? 'RECEPTIONIST')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      if (isEdit) {
        const body: Record<string, string> = {}
        if (username !== user!.username) body.username = username
        if (password) body.password = password
        if (role !== user!.role) body.role = role
        await apiClient.patch(`/users/${user!.id}`, body)
      } else {
        await apiClient.post('/users', { username, password, role })
      }
      onSaved()
    } catch (err) {
      const axiosErr = err as AxiosError<{ detail?: string; title?: string }>
      setError(axiosErr.response?.data?.detail ?? 'Failed to save')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-xl p-6 w-full max-w-md shadow-xl">
        <h2 className="text-xl font-bold mb-4">{isEdit ? 'Edit user' : 'New user'}</h2>
        {error && <div className="bg-red-100 text-red-700 p-3 rounded mb-4 text-sm">{error}</div>}
        <form onSubmit={handleSubmit} className="space-y-3">
          <label className="block">
            <span className="block text-sm text-slate-700 mb-1">Username</span>
            <input type="text" value={username} onChange={(e) => setUsername(e.target.value)} required minLength={3}
              className="w-full border border-slate-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none" />
          </label>
          <label className="block">
            <span className="block text-sm text-slate-700 mb-1">
              Password {isEdit && <span className="text-slate-400">(leave empty to keep)</span>}
            </span>
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} minLength={isEdit ? 0 : 8} required={!isEdit}
              className="w-full border border-slate-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none" />
          </label>
          <label className="block">
            <span className="block text-sm text-slate-700 mb-1">Role</span>
            <select value={role} onChange={(e) => setRole(e.target.value as Role)} disabled={user?.isSystem}
              className="w-full border border-slate-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none disabled:bg-slate-100">
              <option value="ADMIN">Administrator</option>
              <option value="RECEPTIONIST">Receptionist</option>
              <option value="TRAINER">Trainer</option>
              <option value="MEMBER">Member</option>
            </select>
          </label>
          <div className="flex justify-end gap-2 pt-2">
            <button type="button" onClick={onClose} className="px-4 py-2 bg-slate-200 rounded-lg hover:bg-slate-300">Cancel</button>
            <button type="submit" disabled={loading} className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg disabled:opacity-50">
              {loading ? 'Saving...' : 'Save'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}