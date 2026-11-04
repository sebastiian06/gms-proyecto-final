
import { useEffect, useState } from 'react'
import { apiClient } from '../api/client'
import type { Member, Routine, Exercise } from '../types'
import { Dumbbell } from 'lucide-react'

export function MyRoutine() {
  const [routine, setRoutine] = useState<Routine | null>(null)
  const [exercises, setExercises] = useState<Exercise[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function load() {
      try {
        setLoading(true)

        // Obtener el miembro autenticado.
        const memberRes = await apiClient.get<Member>('/members/me')
        const myMember = memberRes.data

        // Obtener su rutina asignada.
        const routineRes = await apiClient.get<Routine>(
          `/routines/members/${myMember.id}`,
        )

        setRoutine(routineRes.data)

        // Obtener los ejercicios de esa rutina.
        const exerciseRes = await apiClient.get<Exercise[]>(
          `/routines/${routineRes.data.id}/exercises`,
        )

        setExercises(exerciseRes.data)
      } catch (err) {
        // Si no tiene rutina asignada, se muestra el mensaje correspondiente.
        console.error('Error loading routine:', err)
        setRoutine(null)
        setExercises([])
      } finally {
        setLoading(false)
      }
    }

    load()
  }, [])

  return (
    <div className="max-w-4xl mx-auto">
      <h1 className="text-3xl font-bold text-slate-800 mb-6 flex items-center gap-2">
        <Dumbbell /> My Routine
      </h1>

      {loading ? (
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-12 text-center text-slate-500">
          Loading routine...
        </div>
      ) : routine ? (
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
          <h2 className="text-xl font-bold text-slate-800 mb-2">
            {routine.name}
          </h2>

          <p className="text-sm text-slate-500 mb-6">
            Assigned by {routine.trainerName}
          </p>

          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-slate-50 border-y border-slate-200">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500">
                    Exercise
                  </th>

                  <th className="px-4 py-3 text-center text-xs font-semibold text-slate-500">
                    Sets
                  </th>

                  <th className="px-4 py-3 text-center text-xs font-semibold text-slate-500">
                    Reps
                  </th>
                </tr>
              </thead>

              <tbody className="divide-y divide-slate-100">
                {exercises.length === 0 && (
                  <tr>
                    <td
                      colSpan={3}
                      className="px-4 py-8 text-center text-slate-500"
                    >
                      No exercises assigned to this routine.
                    </td>
                  </tr>
                )}

                {exercises.map((ex) => (
                  <tr key={ex.id}>
                    <td className="px-4 py-3 font-medium text-slate-800">
                      {ex.name}
                    </td>

                    <td className="px-4 py-3 text-center text-slate-600">
                      {ex.sets}
                    </td>

                    <td className="px-4 py-3 text-center text-slate-600">
                      {ex.repetitions}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-12 text-center text-slate-500">
          You don't have a routine assigned yet.
        </div>
      )}
    </div>
  )
}
