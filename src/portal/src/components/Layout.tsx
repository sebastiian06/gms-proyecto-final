import { Link, Outlet, useNavigate, useLocation } from 'react-router-dom'
import {
  LayoutDashboard,
  Users,
  Package,
  CreditCard,
  Dumbbell,
  UserCog,
  Shield,
  LogOut,
  LogIn,
  Briefcase,
  CalendarCheck,
  User,
} from 'lucide-react'
import { useAuth } from '../auth/AuthContext'

const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard, roles: ['ADMIN', 'RECEPTIONIST'] },
  { to: '/members', label: 'Members', icon: Users, roles: ['ADMIN', 'RECEPTIONIST'] },
  { to: '/plans', label: 'Plans', icon: Package, roles: ['ADMIN', 'RECEPTIONIST', 'TRAINER'] },
  { to: '/memberships', label: 'Payments', icon: CreditCard, roles: ['ADMIN', 'RECEPTIONIST'] },
  { to: '/attendance', label: 'Attendance', icon: CalendarCheck, roles: ['ADMIN', 'RECEPTIONIST'] },
  { to: '/routines', label: 'Routines', icon: Dumbbell, roles: ['ADMIN', 'TRAINER'] },
  { to: '/trainers', label: 'Trainers', icon: Briefcase, roles: ['ADMIN'] },
  { to: '/users', label: 'Users', icon: UserCog, roles: ['ADMIN'] },
  { to: '/roles', label: 'Roles', icon: Shield, roles: ['ADMIN'] },
  { to: '/assigned-members', label: 'My Clients', icon: Users, roles: ['TRAINER'] },
  { to: '/my-profile', label: 'My Profile', icon: User, roles: ['MEMBER'] },
  { to: '/my-routine', label: 'My Routine', icon: Dumbbell, roles: ['MEMBER'] },
  { to: '/my-payments', label: 'My Payments', icon: CreditCard, roles: ['MEMBER'] },
]

export function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  function isActive(path: string) {
    if (path === '/') return location.pathname === '/'
    return location.pathname === path || location.pathname.startsWith(path + '/')
  }

  return (
    <div className="min-h-screen flex bg-slate-100">
      <aside className="w-64 bg-slate-900 text-slate-100 flex flex-col">
        <div className="px-6 py-6 border-b border-slate-800">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-blue-600 flex items-center justify-center font-bold">
              G
            </div>
            <div>
              <h1 className="text-lg font-bold leading-tight">GMS</h1>
              <p className="text-xs text-slate-400">GYM MANAGEMENT</p>
            </div>
          </div>
        </div>

        <nav className="flex-1 px-3 py-4 space-y-1">
          {NAV_ITEMS.filter((item) => user && item.roles.includes(user.role)).map((item) => {
            const Icon = item.icon
            const active = isActive(item.to)
            return (
              <Link
                key={item.to}
                to={item.to}
                className={`flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                  active
                    ? 'bg-blue-600 text-white'
                    : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                }`}
              >
                <Icon size={18} />
                {item.label}
              </Link>
            )
          })}
        </nav>

        <div className="px-3 py-2 border-t border-slate-800">
          <a
            href="/kiosk"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium text-slate-400 hover:bg-slate-800 hover:text-white"
          >
            <LogIn size={18} />
            Open Kiosk
          </a>
        </div>

        <div className="px-3 py-4 border-t border-slate-800">
          <div className="flex items-center gap-3 px-3 py-2">
            <div className="w-9 h-9 rounded-full bg-slate-700 flex items-center justify-center text-sm font-bold">
              {user?.username.substring(0, 2).toUpperCase()}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium truncate">{user?.username}</p>
              <span className="text-xs px-2 py-0.5 bg-blue-600 rounded-full">
                {user?.role}
              </span>
            </div>
            <button
              onClick={handleLogout}
              className="text-slate-400 hover:text-white"
              title="Logout"
            >
              <LogOut size={18} />
            </button>
          </div>
        </div>
      </aside>

      <main className="flex-1 overflow-y-auto">
        <div className="max-w-7xl mx-auto p-8">
          <Outlet />
        </div>
      </main>
    </div>
  )
}