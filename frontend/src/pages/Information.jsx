import { useEffect, useState } from 'react'
import { api } from '../api'
import { Activity, BookOpen, HeartHandshake, Microscope, Stethoscope } from 'lucide-react'

const services=[
 {name:'Khám chuyên khoa theo yêu cầu',price:'Từ 300.000đ',text:'Chủ động chọn bác sĩ, ngày và khung giờ phù hợp.'},
 {name:'Khám sức khỏe tổng quát',price:'Theo gói',text:'Đánh giá toàn diện với danh mục xét nghiệm phù hợp độ tuổi.'},
 {name:'Tư vấn từ xa',price:'Theo chuyên khoa',text:'Hỗ trợ định hướng trước khám qua tổng đài 1900 888 866.'}
]
const articles=[
 {title:'Chuẩn bị gì trước khi đi khám?',text:'Mang giấy tờ tùy thân, đơn thuốc đang dùng và đến trước giờ hẹn 15 phút.'},
 {title:'Theo dõi sức khỏe tim mạch',text:'Kiểm tra huyết áp định kỳ và đi khám khi có đau ngực, khó thở bất thường.'},
 {title:'An toàn khi sử dụng thuốc',text:'Không tự đổi liều hoặc ngừng thuốc khi chưa có hướng dẫn của bác sĩ.'}
]

export default function Information(){
 const [doctors,setDoctors]=useState([]),[error,setError]=useState('')
 useEffect(()=>{api('/api/public/doctors').then(setDoctors).catch(e=>setError(e.message))},[])
 return <div className="page-bg"><div className="container info-page"><div className="page-title"><span className="eyebrow">Cổng thông tin y tế</span><h1>Dịch vụ và đội ngũ chuyên môn</h1><p>Thông tin phục vụ người bệnh trước, trong và sau quá trình thăm khám.</p></div>{error&&<div className="alert error">{error}</div>}
  <section><div className="section-heading"><h2><Stethoscope/> Bác sĩ</h2></div><div className="info-grid">{doctors.map(d=><article className="info-card" key={d.id}><span><Stethoscope/></span><h3>{d.title} {d.fullName}</h3><b>{d.specialtyName} · {d.facilityName}</b><p>{d.bio}</p></article>)}</div></section>
  <section><div className="section-heading"><h2><Activity/> Dịch vụ y tế</h2></div><div className="info-grid">{services.map(x=><article className="info-card" key={x.name}><span><HeartHandshake/></span><h3>{x.name}</h3><b>{x.price}</b><p>{x.text}</p></article>)}</div></section>
  <section><div className="section-heading"><h2><Microscope/> Trang thiết bị</h2></div><div className="info-grid"><article className="info-card"><h3>Chẩn đoán hình ảnh</h3><p>Hệ thống CT, MRI và siêu âm phục vụ chẩn đoán chuyên sâu.</p></article><article className="info-card"><h3>Xét nghiệm tự động</h3><p>Dây chuyền xét nghiệm hiện đại, chuẩn hóa và kiểm soát chất lượng.</p></article><article className="info-card"><h3>Can thiệp chuyên khoa</h3><p>Phòng thủ thuật và can thiệp đáp ứng yêu cầu an toàn bệnh viện.</p></article></div></section>
  <section><div className="section-heading"><h2><BookOpen/> Kiến thức y khoa</h2></div><div className="info-grid">{articles.map(x=><article className="info-card" key={x.title}><h3>{x.title}</h3><p>{x.text}</p></article>)}</div></section>
  <aside className="consult-box"><div><h2>Cần hỗ trợ tư vấn?</h2><p>Liên hệ tổng đài 1900 888 866 hoặc email cskh@bachmai.gov.vn.</p></div><a className="btn btn-primary" href="tel:1900888866">Gọi tư vấn</a></aside>
 </div></div>
}
