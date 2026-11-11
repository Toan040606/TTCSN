import { Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import Home from './pages/Home'
import Booking from './pages/Booking'
import Lookup from './pages/Lookup'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import { auth } from './api'

function Protected({ children }) {
  return auth.get() ? children : <Navigate to="/dang-nhap" replace />
}

export default function App() {
  return <Routes>
    <Route element={<Layout />}>
      <Route index element={<Home />} />
      <Route path="/dat-lich" element={<Booking />} />
      <Route path="/tra-cuu" element={<Lookup />} />
    </Route>
    <Route path="/dang-nhap" element={<Login />} />
    <Route path="/dashboard/*" element={<Protected><Dashboard /></Protected>} />
    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes>
}
