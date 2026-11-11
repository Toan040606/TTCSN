import { Link } from 'react-router-dom'
import { Activity, ArrowRight, Award, Brain, Building2, CalendarCheck, CheckCircle2, Clock3, HeartPulse, MapPin, Search, ShieldCheck, Stethoscope, Users } from 'lucide-react'

const specialties = [
  {icon:HeartPulse, name:'Tim mạch', desc:'Chẩn đoán và điều trị chuyên sâu bệnh lý tim mạch'},
  {icon:Brain, name:'Thần kinh', desc:'Chăm sóc toàn diện thần kinh và đột quỵ'},
  {icon:Activity, name:'Tiêu hóa', desc:'Khám bệnh lý tiêu hóa, gan và đường mật'},
  {icon:Stethoscope, name:'Nội tổng hợp', desc:'Khám, tư vấn và điều trị nội khoa tổng quát'}
]

export default function Home() {
  return <>
    <section className="hero">
      <div className="hero-orb orb-one"/><div className="hero-orb orb-two"/>
      <div className="container hero-grid">
        <div className="hero-copy">
          <span className="eyebrow"><ShieldCheck size={16}/> Cổng đặt lịch chính thức</span>
          <h1>Khám đúng giờ.<br/><em>An tâm hơn.</em></h1>
          <p>Chủ động chọn cơ sở, chuyên khoa, bác sĩ và khung giờ phù hợp. Nhận phiếu hẹn điện tử tức thì, không còn chờ đợi mệt mỏi.</p>
          <div className="hero-actions"><Link className="btn btn-primary btn-lg" to="/dat-lich"><CalendarCheck/> Đặt lịch khám</Link><Link className="btn btn-ghost btn-lg" to="/tra-cuu"><Search/> Tra cứu lịch hẹn</Link></div>
          <div className="trust-row"><div><b>9.000+</b><span>lượt khám/ngày</span></div><div><b>100+</b><span>chuyên gia đầu ngành</span></div><div><b>2</b><span>cơ sở hiện đại</span></div></div>
        </div>
        <div className="hero-visual">
          <div className="hero-card-main">
            <div className="medical-cross">+</div>
            <div className="visual-copy"><span>ĐẶT LỊCH TRỰC TUYẾN</span><h3>Chăm sóc sức khỏe<br/>chỉ trong vài phút</h3><div className="visual-check"><CheckCircle2/> Xác nhận ngay lập tức</div></div>
          </div>
          <div className="floating-card card-time"><Clock3/><div><small>Thời gian chờ</small><b>Giảm đến 70%</b></div></div>
          <div className="floating-card card-safe"><ShieldCheck/><div><small>Dữ liệu</small><b>An toàn & bảo mật</b></div></div>
        </div>
      </div>
    </section>

    <section className="quick-bar"><div className="container quick-grid">
      <Link to="/dat-lich"><span><CalendarCheck/></span><div><b>Đặt lịch khám</b><small>Chọn lịch phù hợp</small></div><ArrowRight/></Link>
      <Link to="/tra-cuu"><span><Search/></span><div><b>Tra cứu hồ sơ</b><small>Qua số điện thoại</small></div><ArrowRight/></Link>
      <a href="tel:1900888866"><span><PhoneIcon/></span><div><b>Gọi tổng đài</b><small>1900 888 866</small></div><ArrowRight/></a>
    </div></section>

    <section className="section container">
      <div className="section-heading"><div><span className="eyebrow">Chuyên môn hàng đầu</span><h2>Chuyên khoa nổi bật</h2></div><Link to="/dat-lich">Xem tất cả <ArrowRight size={17}/></Link></div>
      <div className="specialty-grid">{specialties.map(({icon:Icon,name,desc})=><Link to="/dat-lich" className="specialty-card" key={name}><span><Icon/></span><h3>{name}</h3><p>{desc}</p><b>Tìm lịch khám <ArrowRight size={15}/></b></Link>)}</div>
    </section>

    <section className="section soft-section"><div className="container">
      <div className="center-heading"><span className="eyebrow">Quy trình đơn giản</span><h2>Đặt lịch trong 3 bước</h2><p>Tiết kiệm thời gian, chủ động hành trình khám bệnh của bạn.</p></div>
      <div className="steps"><div><span>01</span><CalendarCheck/><h3>Chọn lịch khám</h3><p>Chọn cơ sở, chuyên khoa, bác sĩ và giờ khám.</p></div><i/><div><span>02</span><CheckCircle2/><h3>Nhận xác nhận</h3><p>Nhận mã hẹn và QR điện tử ngay trên màn hình.</p></div><i/><div><span>03</span><Stethoscope/><h3>Đến khám đúng giờ</h3><p>Quét QR tại quầy để check-in nhanh chóng.</p></div></div>
    </div></section>

    <section className="section container"><div className="facility-feature">
      <div><span className="eyebrow">Hai cơ sở — Một chất lượng</span><h2>Chọn địa điểm thuận tiện nhất</h2><p>Hệ thống lịch khám liên cơ sở giúp phân luồng thông minh và rút ngắn thời gian chờ.</p><div className="facility-list"><div><Building2/><span><b>Cơ sở 1 — Hà Nội</b><small>78 Giải Phóng, Đống Đa, Hà Nội</small></span></div><div><MapPin/><span><b>Cơ sở 2 — Hà Nam</b><small>Phường Liêm Tuyền, TP. Phủ Lý, Hà Nam</small></span></div></div><Link className="btn btn-primary" to="/dat-lich">Chọn cơ sở và đặt lịch <ArrowRight size={17}/></Link></div>
      <div className="facility-art"><div className="building-shape"><span>+</span><b>BẠCH MAI</b></div><div className="art-badge"><Award/><span><b>Bệnh viện hạng đặc biệt</b><small>Tuyến cuối quốc gia</small></span></div></div>
    </div></section>
  </>
}

function PhoneIcon(){ return <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6A19.79 19.79 0 0 1 2.12 4.18 2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.12.9.33 1.78.62 2.63a2 2 0 0 1-.45 2.11L8 9.73a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.85.29 1.73.5 2.63.62A2 2 0 0 1 22 16.92z"/></svg>}
