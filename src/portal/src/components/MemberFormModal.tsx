import { useState, useEffect, type FormEvent } from 'react'
import { apiClient } from '../api/client'
import type { Member, Plan, Trainer, PaymentMethod } from '../types'
import { AxiosError } from 'axios'
import { useAuth } from '../auth/AuthContext'

interface Props {
  member: Member | null
  onClose: () => void
  onSaved: () => void
}

export function MemberFormModal({ member, onClose, onSaved }: Props) {
  const { user } = useAuth()
  const isEdit = !!member
  const isReceptionist = user?.role === 'RECEPTIONIST'

  // Identity + profile fields
  // MemberDto no incluye username, por eso queda vacío en edición.
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [documentNumber, setDocumentNumber] = useState(
    member?.documentNumber ?? '',
  )
  const [fullName, setFullName] = useState(member?.fullName ?? '')
  const [contact, setContact] = useState(member?.contact ?? '')

  // Plan + payment + trainer
  const [plans, setPlans] = useState<Plan[]>([])
  const [trainers, setTrainers] = useState<Trainer[]>([])
  const [planId, setPlanId] = useState<string>('')
  const [currentPlanId, setCurrentPlanId] = useState<number | null>(null)
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('CASH')
  const [initialPaymentAmount, setInitialPaymentAmount] = useState<string>('')
  const [trainerId, setTrainerId] = useState<string>(
    member?.trainerId?.toString() ?? '',
  )

  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [loadingData, setLoadingData] = useState(true)

  // Cargar planes y trainers
  useEffect(() => {
    setLoadingData(true)
    setCurrentPlanId(null)

    Promise.all([
      apiClient.get<{ content: Plan[] }>('/plans', {
        params: { page: 0, size: 100, active: true },
      }),
      apiClient.get<Trainer[]>('/trainers'),
      isEdit
        ? apiClient.get<{ content: any[] }>('/memberships', {
            params: { memberId: member!.id, status: 'ACTIVE' },
          })
        : Promise.resolve(null),
    ])
      .then(([plansRes, trainersRes, memshipRes]) => {
        setPlans(plansRes.data.content)
        setTrainers(trainersRes.data)

        if (isEdit && memshipRes && memshipRes.data.content.length > 0) {
          const activePlanId = Number(
            memshipRes.data.content[0].planId,
          )

          setPlanId(activePlanId.toString())
          setCurrentPlanId(activePlanId)
        } else {
          setPlanId('')
          setCurrentPlanId(null)
        }
      })
      .catch((err) => {
        const axiosErr = err as AxiosError<{ detail?: string }>
        setError(
          axiosErr.response?.data?.detail ??
            'Failed to load plans/trainers',
        )
      })
      .finally(() => setLoadingData(false))
  }, [isEdit, member])

  const selectedPlan = plans.find((p) => p.id === Number(planId))

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)

    try {
      if (isEdit) {
        const body: Record<string, unknown> = {}

        // Solo enviar los campos que realmente cambiaron.
        if (
          contact.trim() !== (member?.contact ?? '').trim()
        ) {
          body.contact = contact.trim()
        }

        // Username y password solo si se escribieron.
        if (username.trim()) {
          body.username = username.trim()
        }

        if (password.trim()) {
          body.password = password.trim()
        }

        // Solo ADMIN puede cambiar nombre y documento.
        if (!isReceptionist) {
          if (
            documentNumber.trim() !==
            (member?.documentNumber ?? '').trim()
          ) {
            body.documentNumber = documentNumber.trim()
          }

          if (
            fullName.trim() !== (member?.fullName ?? '').trim()
          ) {
            body.fullName = fullName.trim()
          }
        }

        // Solo cambiar el plan si se seleccionó uno diferente.
        if (
          planId &&
          Number(planId) !== currentPlanId
        ) {
          body.planId = Number(planId)
        }

        // Enviar siempre el trainer:
        // - ID para mantener o asignar un entrenador.
        // - null para quitar la asignación.
        body.trainerId = trainerId
          ? Number(trainerId)
          : null

        await apiClient.patch(`/members/${member!.id}`, body)
      } else {
        // Lógica de creación
        await apiClient.post('/members', {
          username,
          password,
          documentNumber,
          fullName,
          contact,
          planId: Number(planId),
          initialPaymentMethod: paymentMethod,
          initialPaymentAmount: initialPaymentAmount
            ? Number(initialPaymentAmount)
            : null,
          trainerId: trainerId ? Number(trainerId) : null,
        })
      }

      onSaved()
    } catch (err) {
      const axiosErr = err as AxiosError<{
        detail?: string
        title?: string
      }>

      setError(
        axiosErr.response?.data?.detail ??
          axiosErr.response?.data?.title ??
          'Failed to save',
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 overflow-y-auto py-8">
      <div className="bg-white rounded-xl p-6 w-full max-w-lg shadow-xl">
        <h2 className="text-xl font-bold mb-4">
          {isEdit ? 'Edit member' : 'New member'}
        </h2>

        {error && (
          <div className="bg-red-100 text-red-700 p-3 rounded mb-4 text-sm">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-3">
          {/* USERNAME Y PASSWORD */}
          <label className="block">
            <span className="block text-sm text-gray-700 mb-1">
              Username{' '}
              {isEdit && (
                <span className="text-slate-400">
                  (leave empty to keep current)
                </span>
              )}
            </span>

            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required={!isEdit}
              minLength={3}
              placeholder={isEdit ? 'Leave empty to keep current' : ''}
              disabled={isReceptionist && isEdit}
              autoComplete="off"
              className="w-full border border-gray-300 rounded px-3 py-2 disabled:bg-slate-100 disabled:text-slate-500"
            />
          </label>

          <label className="block">
            <span className="block text-sm text-gray-700 mb-1">
              Password{' '}
              {isEdit && (
                <span className="text-slate-400">
                  (leave empty to keep current)
                </span>
              )}
            </span>

            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required={!isEdit}
              minLength={isEdit ? 0 : 8}
              autoComplete="new-password"
              disabled={isReceptionist && isEdit}
              className="w-full border border-gray-300 rounded px-3 py-2 disabled:bg-slate-100 disabled:text-slate-500"
            />
          </label>

          {/* DOCUMENTO Y NOMBRE */}
          <label className="block">
            <span className="block text-sm text-gray-700 mb-1">
              Document number
            </span>

            <input
              type="text"
              value={documentNumber}
              onChange={(e) => setDocumentNumber(e.target.value)}
              required
              disabled={isReceptionist && isEdit}
              className="w-full border border-gray-300 rounded px-3 py-2 disabled:bg-slate-100 disabled:text-slate-500"
            />
          </label>

          <label className="block">
            <span className="block text-sm text-gray-700 mb-1">
              Full name
            </span>

            <input
              type="text"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              required
              disabled={isReceptionist && isEdit}
              className="w-full border border-gray-300 rounded px-3 py-2 disabled:bg-slate-100 disabled:text-slate-500"
            />
          </label>

          {/* CONTACTO */}
          <label className="block">
            <span className="block text-sm text-gray-700 mb-1">
              Contact
            </span>

            <input
              type="text"
              value={contact}
              onChange={(e) => setContact(e.target.value)}
              required
              className="w-full border border-gray-300 rounded px-3 py-2"
            />
          </label>

          {/* PLAN Y PAGO (solo en creación) */}
          {!isEdit && (
            <div className="border-t border-slate-200 pt-3 mt-3">
              <p className="text-xs font-semibold text-slate-500 uppercase mb-2">
                Plan & initial payment
              </p>

              {loadingData ? (
                <p className="text-sm text-slate-500">
                  Loading plans...
                </p>
              ) : (
                <>
                  <label className="block mb-3">
                    <span className="block text-sm text-gray-700 mb-1">
                      Plan
                    </span>

                    <select
                      value={planId}
                      onChange={(e) => setPlanId(e.target.value)}
                      required
                      className="w-full border border-gray-300 rounded px-3 py-2"
                    >
                      <option value="">Select a plan</option>

                      {plans.map((p) => (
                        <option key={p.id} value={p.id}>
                          {p.name} — ${p.price.toFixed(2)} /{' '}
                          {p.durationDays} days
                        </option>
                      ))}
                    </select>
                  </label>

                  <label className="block mb-3">
                    <span className="block text-sm text-gray-700 mb-1">
                      Payment method
                    </span>

                    <select
                      value={paymentMethod}
                      onChange={(e) =>
                        setPaymentMethod(
                          e.target.value as PaymentMethod,
                        )
                      }
                      required
                      className="w-full border border-gray-300 rounded px-3 py-2"
                    >
                      <option value="CASH">Cash</option>
                      <option value="TRANSFER">Transfer</option>
                      <option value="CARD">Card</option>
                    </select>
                  </label>

                  <label className="block mb-3">
                    <span className="block text-sm text-gray-700 mb-1">
                      Initial payment amount{' '}
                      <span className="text-slate-400">
                        (leave empty to use plan price)
                      </span>
                    </span>

                    <input
                      type="number"
                      step="0.01"
                      min="0"
                      value={initialPaymentAmount}
                      onChange={(e) =>
                        setInitialPaymentAmount(e.target.value)
                      }
                      placeholder={
                        selectedPlan
                          ? selectedPlan.price.toString()
                          : '0.00'
                      }
                      className="w-full border border-gray-300 rounded px-3 py-2"
                    />
                  </label>
                </>
              )}
            </div>
          )}

          {/* EN EDICIÓN: CAMBIO DE PLAN Y TRAINER */}
          {isEdit && (
            <div className="border-t border-slate-200 pt-3 mt-3">
              <p className="text-xs font-semibold text-slate-500 uppercase mb-2">
                Change plan or trainer
              </p>

              <label className="block mb-3">
                <span className="block text-sm text-gray-700 mb-1">
                  Change Plan (optional)
                </span>

                <select
                  value={planId}
                  onChange={(e) => setPlanId(e.target.value)}
                  className="w-full border border-gray-300 rounded px-3 py-2"
                >
                  <option value="">Keep current plan</option>

                  {plans.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.name} — ${p.price.toFixed(2)}
                    </option>
                  ))}
                </select>
              </label>

              <label className="block mb-3">
                <span className="block text-sm text-gray-700 mb-1">
                  Trainer
                </span>

                <select
                  value={trainerId}
                  onChange={(e) => setTrainerId(e.target.value)}
                  className="w-full border border-gray-300 rounded px-3 py-2"
                >
                  <option value="">No trainer</option>

                  {trainers.map((t) => (
                    <option key={t.id} value={t.id}>
                      {t.fullName}
                    </option>
                  ))}
                </select>
              </label>
            </div>
          )}

          <div className="flex justify-end space-x-2 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
            >
              Cancel
            </button>

            <button
              type="submit"
              disabled={loading || loadingData}
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
