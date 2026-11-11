import { useState } from 'react'
import { Link, NavLink, Outlet } from 'react-router-dom'
import { CalendarPlus, Menu, Phone, Search, ShieldCheck, X } from 'lucide-react'

export default function Layout() {
  const [open, setOpen] = useState(false)
  const nav = [{to:'/', label:'Trang chủ'}, {to:'/dat-lich', label:'Đặt lịch khám'}, {to:'/tra-cuu', label:'Tra cứu lịch hẹn'}]
  return <div className="site-shell">
    <div className="topbar"><div className="container topbar-inner"><span><Phone size={14}/> Tổng đài: <b>1900 888 866</b></span><span><ShieldCheck size={14}/> Nền tảng đặt lịch chính thức</span></div></div>
    <header className="header">
      <div className="container nav-wrap">
        <Link className="brand" to="/">
          <span className="brand-mark">BM</span><span><b>BẠCH MAI</b><small>Chăm sóc bằng trái tim</small></span>
        </Link>
        <nav className={open ? 'nav open' : 'nav'}>
          {nav.map(item => <NavLink key={item.to} to={item.to} onClick={()=>setOpen(false)}>{item.label}</NavLink>)}
          <NavLink to="/dang-nhap" onClick={()=>setOpen(false)}>Cổng cán bộ</NavLink>
        </nav>
        <Link className="btn btn-primary nav-cta" to="/dat-lich"><CalendarPlus size={18}/> Đặt lịch ngay</Link>
        <button className="menu-btn" onClick={()=>setOpen(!open)} aria-label="Mở menu">{open?<X/>:<Menu/>}</button>
      </div>
    </header>
    <main><Outlet /></main>
    <footer>
      <div className="container footer-grid">
        <div><div className="brand footer-brand"><span className="brand-mark">BM</span><span><b>BỆNH VIỆN BẠCH MAI</b><small>Uy tín — Chất lượng — Tận tâm</small></span></div><p>Hệ thống hỗ trợ đặt lịch khám trực tuyến liên cơ sở, giúp người bệnh chủ động thời gian và giảm chờ đợi.</p></div>
        <div><h4>Dịch vụ nhanh</h4><Link to="/dat-lich">Đặt lịch khám</Link><Link to="/tra-cuu">Tra cứu lịch hẹn</Link><Link to="/dang-nhap">Cổng cán bộ</Link></div>
        <div><h4>Liên hệ</h4><p>78 Giải Phóng, Đống Đa, Hà Nội</p><p>Hotline: 1900 888 866</p><p>Email: cskh@bachmai.gov.vn</p></div>
      </div>
      <div className="copyright">© 2026 Bệnh viện Bạch Mai · Hệ thống đồ án Nhóm 14</div>
    </footer>
  </div>
}
