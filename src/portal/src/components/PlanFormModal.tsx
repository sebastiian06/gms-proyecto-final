import { useState, type FormEvent } from 'react'
import { apiClient } from '../api/client'
import type { Plan } from '../types'
import { AxiosError } from 'axios'

interface Props {
  plan: Plan | null
  onClose: () => void
  onSaved: () => void
}

export function PlanFormModal({ plan, onClose, onSaved }: Props) {
  const isEdit = !!plan

  const [name, setName] = useState(plan?.name ?? '')
  const [price, setPrice] = useState<string>(plan?.price.toString() ?? '')
  const [durationDays, setDurationDays] = useState<string>(
    plan?.durationDays.toString() ?? ''
  )

  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)

    const payload = {
      name,
      price: Number(price),
      durationDays: Number(durationDays),
    }

    try {
      if (isEdit) {
        await apiClient.put(`/plans/${plan!.id}`, payload)
      } else {
        await apiClient.post('/plans', payload)
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
      <div className="bg-white rounded-lg p-6 w-full max-w-md">
        <h2 className="text-xl font-bold mb-4">
          {isEdit ? 'Edit plan' : 'New plan'}
        </h2>

        {error && (
          <div className="bg-red-100 text-red-700 p-3 rounded mb-4 text-sm">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-3">
          <label className="block">
            <span className="block text-sm text-gray-700 mb-1">Name</span>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              className="w-full border border-gray-300 rounded px-3 py-2"
            />
          </label>
          <label className="block">
            <span className="block text-sm text-gray-700 mb-1">Price</span>
            <input
              type="number"
              step="0.01"
              min="0"
              value={price}
              onChange={(e) => setPrice(e.target.value)}
              required
              className="w-full border border-gray-300 rounded px-3 py-2"
            />
          </label>
          <label className="block">
            <span className="block text-sm text-gray-700 mb-1">Duration (days)</span>
            <input
              type="number"
              min="1"
              value={durationDays}
              onChange={(e) => setDurationDays(e.target.value)}
              required
              className="w-full border border-gray-300 rounded px-3 py-2"
            />
          </label>

          <div className="flex justify-end space-x-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 bg-gray-200 rounded"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded disabled:opacity-50"
            >
              {loading ? 'Saving...' : 'Save'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}