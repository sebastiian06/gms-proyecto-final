export function Roles() {
  const matrix = [
    { feature: 'Members', admin: 'CRUD', receptionist: 'CRU', trainer: 'R', member: '—' },
    { feature: 'Plans', admin: 'CRUD', receptionist: 'R', trainer: 'R', member: 'R' },
    { feature: 'Memberships', admin: 'CRUD', receptionist: 'CR', trainer: 'R', member: 'R (own)' },
    { feature: 'Payments', admin: 'CRUD', receptionist: 'CR', trainer: '—', member: 'R (own)' },
    { feature: 'Trainers', admin: 'CRUD', receptionist: 'R', trainer: '—', member: '—' },
    { feature: 'Routines', admin: 'R', receptionist: '—', trainer: 'CRUD', member: 'R (own)' },
    { feature: 'Users', admin: 'CRUD', receptionist: '—', trainer: '—', member: '—' },
    { feature: 'Reports', admin: 'R', receptionist: 'R', trainer: '—', member: '—' },
  ]

  return (
    <div>
      <h1 className="text-2xl font-bold text-slate-800 mb-6">Roles and permissions</h1>
      <div className="bg-white rounded-xl shadow-sm overflow-hidden">
        <table className="w-full">
          <thead className="bg-slate-50 border-b border-slate-200">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">Feature</th>
              <th className="px-4 py-3 text-center text-xs font-semibold text-slate-500 uppercase">Admin</th>
              <th className="px-4 py-3 text-center text-xs font-semibold text-slate-500 uppercase">Receptionist</th>
              <th className="px-4 py-3 text-center text-xs font-semibold text-slate-500 uppercase">Trainer</th>
              <th className="px-4 py-3 text-center text-xs font-semibold text-slate-500 uppercase">Member</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200">
            {matrix.map((row) => (
              <tr key={row.feature}>
                <td className="px-4 py-3 font-medium">{row.feature}</td>
                <td className="px-4 py-3 text-center text-sm">{row.admin}</td>
                <td className="px-4 py-3 text-center text-sm">{row.receptionist}</td>
                <td className="px-4 py-3 text-center text-sm">{row.trainer}</td>
                <td className="px-4 py-3 text-center text-sm">{row.member}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <p className="mt-4 text-sm text-slate-500">
        C = Create, R = Read, U = Update, D = Deactivate, — = no access
      </p>
    </div>
  )
}