
import { useEffect, useState } from 'react'
import { apiClient } from '../api/client'
import type { Plan, Page } from '../types'
import { PlanFormModal } from '../components/PlanFormModal'
import { AxiosError } from 'axios'
import { useAuth } from '../auth/AuthContext'
import { Plus } from 'lucide-react'

export function Plans() {
  const { user } = useAuth()
  const canEdit = user?.role === 'ADMIN'

  const [data, setData] = useState<Page<Plan> | null>(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState<Plan | null>(null)

  async function load() {
    setLoading(true)
    setError(null)

    try {
      const { data } = await apiClient.get<Page<Plan>>('/plans', {
        params: { page, size: 20 },
      })

      setData(data)
    } catch (err) {
      const axiosErr = err as AxiosError<{
        detail?: string
        title?: string
      }>

      setError(
        axiosErr.response?.data?.detail ?? 'Failed to load plans'
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [page])

  async function handleDeactivate(id: number) {
    if (!confirm('Deactivate this plan?')) return

    try {
      await apiClient.delete(`/plans/${id}`)
      load()
    } catch (err) {
      const axiosErr = err as AxiosError<{ detail?: string }>

      alert(
        axiosErr.response?.data?.detail ??
          'Failed to deactivate plan'
      )
    }
  }

  async function handleActivate(id: number) {
    try {
      await apiClient.patch(`/plans/${id}/activate`)
      load()
    } catch (err) {
      const axiosErr = err as AxiosError<{ detail?: string }>

      alert(
        axiosErr.response?.data?.detail ??
          'Failed to activate plan'
      )
    }
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-2xl font-bold text-gray-800">
          Plans
        </h1>

        {/* Solo ADMIN puede crear planes */}
        {canEdit && (
          <button
            onClick={() => {
              setEditing(null)
              setShowForm(true)
            }}
            className="flex items-center gap-2 bg-blue-700 hover:bg-blue-800 text-white px-5 py-2.5 rounded-lg font-medium shadow-sm transition-colors"
          >
            <Plus size={18} /> New Plan
          </button>
        )}
      </div>

      {error && (
        <div className="bg-red-100 text-red-700 p-3 rounded mb-4">
          {error}
        </div>
      )}

      {loading && <p>Loading...</p>}

      {data && (
        <>
          <div className="bg-white rounded-lg shadow overflow-hidden">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">
                    Name
                  </th>

                  <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">
                    Price
                  </th>

                  <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">
                    Duration (days)
                  </th>

                  <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">
                    Status
                  </th>

                  <th className="px-4 py-2 text-right text-xs font-medium text-gray-500 uppercase">
                    Actions
                  </th>
                </tr>
              </thead>

              <tbody className="divide-y divide-gray-200">
                {data.content.length === 0 && (
                  <tr>
                    <td
                      colSpan={5}
                      className="px-4 py-6 text-center text-gray-500"
                    >
                      No plans yet
                    </td>
                  </tr>
                )}

                {data.content.map((p) => (
                  <tr key={p.id}>
                    <td className="px-4 py-2">
                      {p.name}
                    </td>

                    <td className="px-4 py-2">
                      ${p.price.toFixed(2)}
                    </td>

                    <td className="px-4 py-2">
                      {p.durationDays}
                    </td>

                    <td className="px-4 py-2">
                      {p.active ? (
                        <span className="text-green-600 font-medium">
                          Active
                        </span>
                      ) : (
                        <span className="text-red-600 font-medium">
                          Inactive
                        </span>
                      )}
                    </td>

                    <td className="px-6 py-4 text-right space-x-2">
                      {/* Solo ADMIN puede editar o cambiar el estado */}
                      {canEdit && (
                        <>
                          <button
                            onClick={() => {
                              setEditing(p)
                              setShowForm(true)
                            }}
                            className="inline-flex items-center justify-center px-4 py-1.5 border border-slate-300 rounded-lg text-sm font-medium text-slate-700 bg-white hover:bg-slate-50 transition-colors"
                          >
                            Edit
                          </button>

                          {p.active ? (
                            <button
                              onClick={() => handleDeactivate(p.id)}
                              className="inline-flex items-center justify-center px-4 py-1.5 rounded-lg text-sm font-medium text-white bg-red-600 hover:bg-red-700 transition-colors shadow-sm"
                            >
                              Deactivate
                            </button>
                          ) : (
                            <button
                              onClick={() => handleActivate(p.id)}
                              className="inline-flex items-center justify-center px-4 py-1.5 rounded-lg text-sm font-medium text-white bg-green-600 hover:bg-green-700 transition-colors shadow-sm"
                            >
                              Activate
                            </button>
                          )}
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {data.page.totalPages > 1 && (
            <div className="flex justify-between items-center mt-4">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="px-4 py-2 bg-gray-200 rounded disabled:opacity-50"
              >
                Previous
              </button>

              <span className="text-sm text-gray-600">
                Page {data.page.number + 1} of {data.page.totalPages}
              </span>

              <button
                onClick={() => setPage((p) => p + 1)}
                disabled={page >= data.page.totalPages - 1}
                className="px-4 py-2 bg-gray-200 rounded disabled:opacity-50"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}

      {/* El formulario solo se renderiza para ADMIN */}
      {canEdit && showForm && (
        <PlanFormModal
          plan={editing}
          onClose={() => setShowForm(false)}
          onSaved={() => {
            setShowForm(false)
            load()
          }}
        />
      )}
    </div>
  )
}
