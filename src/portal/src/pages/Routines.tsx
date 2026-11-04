import { useEffect, useState } from 'react'
import { apiClient } from '../api/client'
import type { Routine, Exercise, Member, Trainer } from '../types'
import { Plus, Edit2, Trash2, Dumbbell, UserPlus } from 'lucide-react'
import { useAuth } from '../auth/AuthContext'
import { AxiosError } from 'axios'

function errorMessage(err: unknown, fallback: string) {
  const axiosErr = err as AxiosError<{ detail?: string; title?: string }>
  return axiosErr.response?.data?.detail ?? axiosErr.response?.data?.title ?? fallback
}

export function Routines() {
  const { user } = useAuth()
  const isAdmin = user?.role === 'ADMIN'

  const [routines, setRoutines] = useState<Routine[]>([])
  const [trainers, setTrainers] = useState<Trainer[]>([])
  const [myMembers, setMyMembers] = useState<Member[]>([])
  const [myTrainerId, setMyTrainerId] = useState<number | null>(null)
  const [trainerFilter, setTrainerFilter] = useState('')

  // Modals state
  const [showRoutineModal, setShowRoutineModal] = useState(false)
  const [editingRoutine, setEditingRoutine] = useState<Routine | null>(null)
  const [routineName, setRoutineName] = useState('')
  const [formTrainerId, setFormTrainerId] = useState('')

  const [selectedRoutine, setSelectedRoutine] = useState<Routine | null>(null)
  const [exercises, setExercises] = useState<Exercise[]>([])
  const [showExerciseModal, setShowExerciseModal] = useState(false)
  const [editingExercise, setEditingExercise] = useState<Exercise | null>(null)
  const [exerciseForm, setExerciseForm] = useState({ name: '', sets: 3, repetitions: 10 })

  const [showAssignModal, setShowAssignModal] = useState(false)
  const [selectedMemberId, setSelectedMemberId] = useState('')

  async function loadData() {
    try {
      const [tRes, mRes] = await Promise.all([
        apiClient.get<Trainer[]>('/trainers'),
        apiClient.get<{ content: Member[] }>('/members', {
          params: { page: 0, size: 100, active: true },
        }),
      ])
      setTrainers(tRes.data)
      setMyMembers(mRes.data.content)

      let trainerIds: number[] = []
      if (user?.role === 'TRAINER') {
        const current = tRes.data.find((t) => t.userId === user.id)
        setMyTrainerId(current?.id ?? null)
        trainerIds = current ? [current.id] : []
      } else {
        setMyTrainerId(null)
        trainerIds = trainerFilter
          ? [Number(trainerFilter)]
          : tRes.data.map((t) => t.id)
      }

      const results = await Promise.all(
        trainerIds.map((id) => apiClient.get<Routine[]>(`/routines/trainers/${id}`))
      )
      setRoutines(results.flatMap((r) => r.data))
    } catch (err) {
      console.error(err)
    }
  }

  useEffect(() => { loadData() }, [user, trainerFilter])

  function openNewRoutine() {
    setEditingRoutine(null)
    setRoutineName('')
    setFormTrainerId(trainerFilter || (trainers[0] ? String(trainers[0].id) : ''))
    setShowRoutineModal(true)
  }

  async function handleSaveRoutine(e: React.FormEvent) {
    e.preventDefault()
    const trainerId = isAdmin ? Number(formTrainerId) : myTrainerId
    if (!editingRoutine && !trainerId) return alert('Trainer profile not found')

    try {
      if (editingRoutine) {
        await apiClient.put(`/routines/${editingRoutine.id}`, { name: routineName })
      } else {
        await apiClient.post(`/routines/trainers/${trainerId}`, { name: routineName })
      }
      setShowRoutineModal(false)
      setEditingRoutine(null)
      setRoutineName('')
      loadData()
    } catch (err) {
      console.error(err)
      alert(errorMessage(err, 'Failed to save routine'))
    }
  }

  async function handleDeleteRoutine(id: number) {
    if (!confirm('Delete this routine?')) return
    try {
      await apiClient.delete(`/routines/${id}`)
      if (selectedRoutine?.id === id) setSelectedRoutine(null)
      loadData()
    } catch (err) {
      alert(errorMessage(err, 'Failed to delete'))
    }
  }

  async function loadExercises(routine: Routine) {
    setSelectedRoutine(routine)
    try {
      const { data } = await apiClient.get<Exercise[]>(`/routines/${routine.id}/exercises`)
      setExercises(data)
    } catch (err) { console.error(err) }
  }

  async function handleSaveExercise(e: React.FormEvent) {
    e.preventDefault()
    if (!selectedRoutine) return
    try {
      if (editingExercise) {
        await apiClient.put(`/routines/${selectedRoutine.id}/exercises/${editingExercise.id}`, exerciseForm)
      } else {
        await apiClient.post(`/routines/${selectedRoutine.id}/exercises`, exerciseForm)
      }
      setShowExerciseModal(false)
      setEditingExercise(null)
      setExerciseForm({ name: '', sets: 3, repetitions: 10 })
      loadExercises(selectedRoutine)
    } catch (err) {
      alert(errorMessage(err, 'Failed to save exercise'))
    }
  }

  async function handleDeleteExercise(id: number) {
    if (!selectedRoutine || !confirm('Delete exercise?')) return
    try {
      await apiClient.delete(`/routines/${selectedRoutine.id}/exercises/${id}`)
      loadExercises(selectedRoutine)
    } catch (err) {
      alert(errorMessage(err, 'Failed to delete'))
    }
  }

  async function handleAssignRoutine(e: React.FormEvent) {
    e.preventDefault()
    if (!selectedRoutine || !selectedMemberId) return
    try {
      await apiClient.post(`/routines/${selectedRoutine.id}/assign/${selectedMemberId}`)
      setShowAssignModal(false)
      setSelectedMemberId('')
      alert('Routine assigned to member successfully!')
    } catch (err) {
      console.error(err)
      alert(errorMessage(err, 'Failed to assign routine'))
    }
  }

  // Only members assigned to the routine's trainer can receive it
  const assignableMembers = selectedRoutine
    ? myMembers.filter((m) => m.trainerId === selectedRoutine.trainerId)
    : []

  return (
    <div className="flex gap-6 h-[calc(100vh-8rem)]">
      {/* Lista de Rutinas */}
      <div className="w-1/3 flex flex-col">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-bold text-slate-800">Routines</h2>
          <button onClick={openNewRoutine} className="bg-blue-600 hover:bg-blue-700 text-white p-2 rounded-lg shadow-sm">
            <Plus size={16} />
          </button>
        </div>

        {isAdmin && (
          <select
            value={trainerFilter}
            onChange={(e) => setTrainerFilter(e.target.value)}
            className="mb-3 border border-slate-300 rounded-lg px-3 py-2 text-sm"
          >
            <option value="">All trainers</option>
            {trainers.map((t) => <option key={t.id} value={t.id}>{t.fullName}</option>)}
          </select>
        )}

        <div className="space-y-2 overflow-y-auto pr-2">
          {routines.length === 0 && <p className="text-sm text-slate-500">No routines yet.</p>}
          {routines.map(r => (
            <div key={r.id} onClick={() => loadExercises(r)} className={`p-4 rounded-xl cursor-pointer border transition-colors ${selectedRoutine?.id === r.id ? 'bg-blue-50 border-blue-300' : 'bg-white border-slate-200 hover:border-blue-300 shadow-sm'}`}>
              <div className="flex justify-between items-start">
                <div>
                  <p className="font-semibold text-slate-800">{r.name}</p>
                  <p className="text-xs text-slate-500">By {r.trainerName}</p>
                </div>
                <div className="flex gap-1" onClick={e => e.stopPropagation()}>
                  <button onClick={() => { setEditingRoutine(r); setRoutineName(r.name); setShowRoutineModal(true) }} className="text-slate-400 hover:text-blue-600 p-1"><Edit2 size={14} /></button>
                  <button onClick={() => handleDeleteRoutine(r.id)} className="text-slate-400 hover:text-red-600 p-1"><Trash2 size={14} /></button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Detalle de la Rutina */}
      <div className="flex-1">
        {selectedRoutine ? (
          <div className="bg-white rounded-xl shadow-sm p-6 border border-slate-200">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold text-slate-800">{selectedRoutine.name}</h2>
              <button onClick={() => setShowAssignModal(true)} className="flex items-center gap-2 bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg text-sm font-medium shadow-sm">
                <UserPlus size={16} /> Assign to member
              </button>
            </div>

            <div className="flex justify-between items-center mb-4">
              <h3 className="font-semibold text-slate-700">Exercises</h3>
              <button onClick={() => { setEditingExercise(null); setExerciseForm({ name: '', sets: 3, repetitions: 10 }); setShowExerciseModal(true) }} className="text-blue-600 text-sm font-medium flex items-center gap-1 hover:underline">
                <Plus size={14} /> Add exercise
              </button>
            </div>

            <table className="w-full">
              <thead className="bg-slate-50 border-y border-slate-200">
                <tr>
                  <th className="px-4 py-2 text-left text-xs font-semibold text-slate-500">Exercise</th>
                  <th className="px-4 py-2 text-center text-xs font-semibold text-slate-500">Sets</th>
                  <th className="px-4 py-2 text-center text-xs font-semibold text-slate-500">Reps</th>
                  <th className="px-4 py-2 text-right text-xs font-semibold text-slate-500">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {exercises.length === 0 && (
                  <tr><td colSpan={4} className="text-center py-4 text-slate-400 text-sm">No exercises yet</td></tr>
                )}
                {exercises.map(ex => (
                  <tr key={ex.id} className="hover:bg-slate-50">
                    <td className="px-4 py-3 font-medium text-slate-800">{ex.name}</td>
                    <td className="px-4 py-3 text-center text-slate-600">{ex.sets}</td>
                    <td className="px-4 py-3 text-center text-slate-600">{ex.repetitions}</td>
                    <td className="px-4 py-3 text-right space-x-2">
                      <button onClick={() => { setEditingExercise(ex); setExerciseForm({ name: ex.name, sets: ex.sets, repetitions: ex.repetitions }); setShowExerciseModal(true) }} className="text-slate-400 hover:text-blue-600"><Edit2 size={14} /></button>
                      <button onClick={() => handleDeleteExercise(ex.id)} className="text-slate-400 hover:text-red-600"><Trash2 size={14} /></button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="bg-white rounded-xl shadow-sm p-12 text-center text-slate-400 flex flex-col items-center border border-slate-200 h-full justify-center">
            <Dumbbell size={48} className="mb-4 text-slate-300" />
            <p>Select a routine to view its exercises</p>
          </div>
        )}
      </div>

      {/* Modal de Rutina */}
      {showRoutineModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-full max-w-sm shadow-xl">
            <h2 className="text-lg font-bold mb-4">{editingRoutine ? 'Edit Routine' : 'New Routine'}</h2>
            <form onSubmit={handleSaveRoutine} className="space-y-3">
              {isAdmin && !editingRoutine && (
                <select
                  value={formTrainerId}
                  onChange={(e) => setFormTrainerId(e.target.value)}
                  required
                  className="w-full border border-slate-300 rounded px-3 py-2 focus:ring-2 focus:ring-blue-500 outline-none"
                >
                  <option value="">Choose a trainer...</option>
                  {trainers.map((t) => <option key={t.id} value={t.id}>{t.fullName}</option>)}
                </select>
              )}
              <input type="text" placeholder="Routine Name (e.g. Full Body)" value={routineName} onChange={(e) => setRoutineName(e.target.value)} required className="w-full border border-slate-300 rounded px-3 py-2 focus:ring-2 focus:ring-blue-500 outline-none" />
              <div className="flex justify-end gap-2 pt-2">
                <button type="button" onClick={() => setShowRoutineModal(false)} className="px-4 py-2 bg-slate-200 hover:bg-slate-300 rounded text-slate-700">Cancel</button>
                <button type="submit" className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded">Save</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal de Ejercicio */}
      {showExerciseModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-full max-w-sm shadow-xl">
            <h2 className="text-lg font-bold mb-4">{editingExercise ? 'Edit Exercise' : 'New Exercise'}</h2>
            <form onSubmit={handleSaveExercise} className="space-y-3">
              <input type="text" placeholder="Exercise Name" value={exerciseForm.name} onChange={(e) => setExerciseForm({...exerciseForm, name: e.target.value})} required className="w-full border border-slate-300 rounded px-3 py-2 focus:ring-2 focus:ring-blue-500 outline-none" />
              <div className="flex gap-2">
                <input type="number" placeholder="Sets" value={exerciseForm.sets} onChange={(e) => setExerciseForm({...exerciseForm, sets: Number(e.target.value)})} required min="1" className="w-1/2 border border-slate-300 rounded px-3 py-2 focus:ring-2 focus:ring-blue-500 outline-none" />
                <input type="number" placeholder="Reps" value={exerciseForm.repetitions} onChange={(e) => setExerciseForm({...exerciseForm, repetitions: Number(e.target.value)})} required min="1" className="w-1/2 border border-slate-300 rounded px-3 py-2 focus:ring-2 focus:ring-blue-500 outline-none" />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button type="button" onClick={() => setShowExerciseModal(false)} className="px-4 py-2 bg-slate-200 hover:bg-slate-300 rounded text-slate-700">Cancel</button>
                <button type="submit" className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded">Save</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal de Asignar */}
      {showAssignModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-full max-w-sm shadow-xl">
            <h2 className="text-lg font-bold mb-4">Assign: {selectedRoutine?.name}</h2>
            <form onSubmit={handleAssignRoutine} className="space-y-3">
              <label className="block">
                <span className="text-sm text-slate-700 mb-1">Select a member of {selectedRoutine?.trainerName}</span>
                <select value={selectedMemberId} onChange={(e) => setSelectedMemberId(e.target.value)} required className="w-full border border-slate-300 rounded px-3 py-2 focus:ring-2 focus:ring-blue-500 outline-none">
                  <option value="">Select a member</option>
                  {assignableMembers.map(m => <option key={m.id} value={m.id}>{m.fullName} ({m.documentNumber})</option>)}
                </select>
              </label>
              {assignableMembers.length === 0 && (
                <p className="text-xs text-amber-600">
                  This trainer has no assigned members. Assign members from the Trainers page first.
                </p>
              )}
              <div className="flex justify-end gap-2 pt-2">
                <button type="button" onClick={() => setShowAssignModal(false)} className="px-4 py-2 bg-slate-200 hover:bg-slate-300 rounded text-slate-700">Cancel</button>
                <button type="submit" className="px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded">Assign</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}