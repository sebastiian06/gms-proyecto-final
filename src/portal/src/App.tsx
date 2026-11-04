import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './auth/AuthContext'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { Layout } from './components/Layout'
import { Login } from './pages/Login'
import { Dashboard } from './pages/Dashboard'
import { Members } from './pages/Members'
import { MemberDetail } from './pages/MemberDetail'
import { Plans } from './pages/Plans'
import { Payments } from './pages/Payments'
import { Routines } from './pages/Routines'
import { Trainers } from './pages/Trainers'
import { Users } from './pages/Users'
import { Roles } from './pages/Roles'
import { Kiosk } from './pages/Kiosk'
import { Attendance } from './pages/Attendance'
import { MyProfile } from './pages/MyProfile'
import { AssignedMembers } from './pages/AssignedMembers'
import { MyRoutine } from './pages/MyRoutine'
import { MyPayments } from './pages/MyPayments'

// Este componente decide qué mostrar en la ruta "/"
function HomeRedirect() {
  const { user } = useAuth()
  
  // Si es un cliente, lo mandamos a su perfil
  if (user?.role === 'MEMBER') {
    return <Navigate to="/my-profile" replace />
  }

  // Si es un entrenador, lo mandamos a sus clientes asignados
  if (user?.role === 'TRAINER') {
    return <Navigate to="/assigned-members" replace />
  }
  
  // Si es admin o recepcionista, ven el Dashboard
  return <Dashboard />
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/kiosk" element={<Kiosk />} />

          {/* Protected routes */}
          <Route element={<ProtectedRoute />}>
            <Route element={<Layout />}>
              <Route path="/" element={<HomeRedirect />} />
              <Route path="/members" element={<Members />} />
              <Route path="/members/:id" element={<MemberDetail />} />
              <Route path="/plans" element={<Plans />} />
              <Route path="/memberships" element={<Payments />} />
              <Route path="/routines" element={<Routines />} />
              <Route path="/trainers" element={<Trainers />} />
              <Route path="/users" element={<Users />} />
              <Route path="/roles" element={<Roles />} />
              <Route path="/attendance" element={<Attendance />} />
              <Route path="/my-profile" element={<MyProfile />} />
              <Route path="/assigned-members" element={<AssignedMembers />} />
              <Route path="/my-routine" element={<MyRoutine />} />
              <Route path="/my-payments" element={<MyPayments />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Route>
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}

export default App