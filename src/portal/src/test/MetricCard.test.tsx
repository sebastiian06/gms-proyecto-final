import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { Users as UsersIcon } from 'lucide-react'

// Replicamos el componente MetricCard del Dashboard para probarlo de forma aislada
function MetricCard({ label, value, icon: Icon, color }: any) {
  const colorClasses = {
    blue: 'bg-blue-100 text-blue-600',
    green: 'bg-green-100 text-green-600',
    amber: 'bg-amber-100 text-amber-600',
    purple: 'bg-purple-100 text-purple-600',
  }
  return (
    <div className="bg-white rounded-xl p-6 shadow-sm">
      <div className="flex justify-between items-start">
        <p className="text-sm text-slate-500">{label}</p>
        <div className={`p-2 rounded-lg ${colorClasses[color]}`}>
          <Icon size={18} />
        </div>
      </div>
      <p className="text-3xl font-bold text-slate-800 mt-2">{value}</p>
    </div>
  )
}

describe('MetricCard', () => {
  it('renders the label and value correctly', () => {
    render(
      <MetricCard
        label="Active members"
        value={15}
        icon={UsersIcon}
        color="blue"
      />
    )

    expect(screen.getByText('Active members')).toBeInTheDocument()
    expect(screen.getByText('15')).toBeInTheDocument()
  })

  it('renders a string value correctly', () => {
    render(
      <MetricCard
        label="Monthly revenue"
        value="$75,000"
        icon={UsersIcon}
        color="green"
      />
    )

    expect(screen.getByText('Monthly revenue')).toBeInTheDocument()
    expect(screen.getByText('$75,000')).toBeInTheDocument()
  })
})