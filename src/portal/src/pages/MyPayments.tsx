
import { useEffect, useState } from 'react'
import { apiClient } from '../api/client'
import type { Member, Membership, PaymentRecord, Plan, Page } from '../types'
import { CreditCard } from 'lucide-react'

export function MyPayments() {
  const [payments, setPayments] = useState<PaymentRecord[]>([])
  const [plan, setPlan] = useState<Plan | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function load() {
      try {
        setLoading(true)
        setError(null)

        // Obtener el perfil del miembro autenticado.
        const memberRes = await apiClient.get<Member>('/members/me')
        const myMember = memberRes.data

        // Consultar el historial completo de pagos.
        const payRes = await apiClient.get<PaymentRecord[]>(
          `/payments/members/${myMember.id}`,
        )

        const sortedPayments = [...payRes.data].sort(
          (a, b) =>
            new Date(b.recordedDate).getTime() -
            new Date(a.recordedDate).getTime(),
        )

        setPayments(sortedPayments)

        // Obtener el plan de la membresía activa, si existe.
        const memshipsRes = await apiClient.get<Page<Membership>>(
          '/memberships',
          {
            params: {
              memberId: myMember.id,
              status: 'ACTIVE',
            },
          },
        )

        const activeMemship = memshipsRes.data.content.find(
          (membership) => membership.status === 'ACTIVE',
        )

        if (activeMemship) {
          const pRes = await apiClient.get<Plan>(
            `/plans/${activeMemship.planId}`,
          )

          setPlan(pRes.data)
        }
      } catch (err) {
        console.error('Error loading payment history:', err)
        setError('Unable to load your payment history.')
      } finally {
        setLoading(false)
      }
    }

    load()
  }, [])

  return (
    <div className="max-w-4xl mx-auto">
      <h1 className="text-3xl font-bold text-slate-800 mb-6 flex items-center gap-2">
        <CreditCard /> My Payments
      </h1>

      <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
        {loading ? (
          <p className="py-8 text-center text-slate-500">
            Loading payment history...
          </p>
        ) : error ? (
          <p className="py-8 text-center text-red-600">
            {error}
          </p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="border-b border-slate-100">
                <tr>
                  <th className="text-left pb-3 text-xs font-semibold text-slate-400 uppercase">
                    Current plan
                  </th>

                  <th className="text-left pb-3 text-xs font-semibold text-slate-400 uppercase">
                    Amount
                  </th>

                  <th className="text-left pb-3 text-xs font-semibold text-slate-400 uppercase">
                    Method
                  </th>

                  <th className="text-right pb-3 text-xs font-semibold text-slate-400 uppercase">
                    Date
                  </th>
                </tr>
              </thead>

              <tbody className="divide-y divide-slate-50">
                {payments.length === 0 && (
                  <tr>
                    <td
                      colSpan={4}
                      className="py-8 text-center text-slate-500"
                    >
                      No payments recorded yet.
                    </td>
                  </tr>
                )}

                {payments.map((p) => (
                  <tr key={p.id}>
                    <td className="py-4 text-sm text-slate-800">
                      {plan?.name ?? '—'}
                    </td>

                    <td className="py-4 text-sm font-semibold text-slate-800">
                      ${p.amount.toFixed(2)}
                    </td>

                    <td className="py-4 text-sm text-slate-600">
                      <span className="px-2 py-1 bg-slate-100 rounded text-xs">
                        {p.method}
                      </span>
                    </td>

                    <td className="py-4 text-right text-sm text-slate-500">
                      {p.recordedDate}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
