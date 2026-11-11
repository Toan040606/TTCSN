import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { QRCodeSVG } from 'qrcode.react'
import { api } from '../api'
import { Check, Loader2, X } from 'lucide-react'

const initial={fullName:'',phone:'',email:'',dateOfBirth:'',gender:'',address:'',facilityId:'',specialtyId:'',doctorId:'',appointmentDate:'',appointmentTime:'',reason:''}

export default function Booking(){
 const [form,setForm]=useState(initial),[facilities,setFacilities]=useState([]),[specialties,setSpecialties]=useState([]),[doctors,setDoctors]=useState([]),[slots,setSlots]=useState([]),[loading,setLoading]=useState(false),[error,setError]=useState(''),[result,setResult]=useState(null)
 useEffect(()=>{Promise.all([api('/api/public/facilities'),api('/api/public/specialties')]).then(([f,s])=>{setFacilities(f);setSpecialties(s);if(f[0])setForm(v=>({...v,facilityId:String(f[0].id)}))}).catch(e=>setError(e.message))},[])
 useEffect(()=>{if(form.facilityId&&form.specialtyId)api(`/api/public/doctors?facilityId=${form.facilityId}&specialtyId=${form.specialtyId}`).then(setDoctors).catch(e=>setError(e.message));else setDoctors([])},[form.facilityId,form.specialtyId])
 useEffect(()=>{if(form.doctorId&&form.appointmentDate)api(`/api/public/doctors/${form.doctorId}/slots?date=${form.appointmentDate}`).then(setSlots).catch(e=>setError(e.message));else setSlots([])},[form.doctorId,form.appointmentDate])
 const set=(k,v)=>{setForm(x=>({...x,[k]:v}));setError('')}
 const submit=async e=>{e.preventDefault();setLoading(true);setError('');try{setResult(await api('/api/public/appointments',{method:'POST',body:JSON.stringify({...form,facilityId:+form.facilityId,specialtyId:+form.specialtyId,doctorId:+form.doctorId,dateOfBirth:form.dateOfBirth||null})}))}catch(e){setError(e.message)}finally{setLoading(false)}}
 return <div className="figma-page"><form className="figma-form booking-figma" onSubmit={submit} data-node-id="29:2">
   <span className="uc-tag blue">USE CASE 3.2: ĐẶT LỊCH KHÁM TRỰC TUYẾN (BỆNH NHÂN)</span>
   <h1>Đăng Ký Đặt Lịch Khám Bệnh Trực Tuyến</h1>
   <p className="figma-lead">Vui lòng điền đầy đủ thông tin để hệ thống sắp xếp bác sĩ chuyên khoa và tạo mã tiếp đón điện tử nhanh chóng.</p>
   {error&&<div className="alert error">{error}</div>}
   <div className="facility-pills">{facilities.map((x,i)=><button type="button" className={form.facilityId==x.id?'active':''} onClick={()=>{set('facilityId',String(x.id));set('doctorId','')}} key={x.id}>{form.facilityId==x.id?'●':'○'} {i===0?'Cơ sở 1: 78 Giải Phóng, Đống Đa, Hà Nội':'Cơ sở 2: Thành phố Phủ Lý, Hà Nam'}</button>)}</div>
   <div className="figma-fields two"><label>Họ và tên bệnh nhân (*)<input required value={form.fullName} onChange={e=>set('fullName',e.target.value)} placeholder="Nguyễn Văn A"/></label><label>Số điện thoại liên hệ (*)<input required pattern="(0|\+84)[0-9]{9}" value={form.phone} onChange={e=>set('phone',e.target.value)} placeholder="0988 123 456"/></label></div>
   <div className="figma-fields two"><label>Địa chỉ Email nhận phiếu hẹn (*)<input required type="email" value={form.email} onChange={e=>set('email',e.target.value)} placeholder="email@example.com"/></label><label>Ngày khám mong muốn (*)<input required type="date" min={new Date().toISOString().split('T')[0]} value={form.appointmentDate} onChange={e=>{set('appointmentDate',e.target.value);set('appointmentTime','')}}/></label></div>
   <div className="figma-fields two"><label>Chọn chuyên khoa khám (*)<select required value={form.specialtyId} onChange={e=>{set('specialtyId',e.target.value);set('doctorId','')}}><option value="">— Chọn chuyên khoa —</option>{specialties.map(x=><option value={x.id} key={x.id}>{x.name}</option>)}</select></label><label>Bác sĩ yêu cầu (hoặc phân công tự động)<select required value={form.doctorId} onChange={e=>set('doctorId',e.target.value)}><option value="">— Chọn bác sĩ —</option>{doctors.map(x=><option value={x.id} key={x.id}>{x.title} {x.fullName}</option>)}</select></label></div>
   <label>Chọn khung giờ khám (*)</label><div className="figma-slots">{slots.map(t=><button type="button" className={form.appointmentTime===t?'active':''} onClick={()=>set('appointmentTime',t)} key={t}>{t.slice(0,5)} - {addHour(t)}</button>)}{form.appointmentDate&&form.doctorId&&!slots.length&&<small>Không còn khung giờ trống.</small>}</div>
   <label>Lý do khám / Triệu chứng lâm sàng ban đầu (*)<textarea required rows="2" value={form.reason} onChange={e=>set('reason',e.target.value)} placeholder="Mô tả tình trạng sức khỏe cần thăm khám..."/></label>
   <button className="figma-primary" disabled={loading||!form.appointmentTime}>{loading?<Loader2 className="spin"/>:'Hoàn tất đặt lịch khám'}</button>
 </form>{result&&<SuccessModal data={result} onClose={()=>setResult(null)}/>}</div>
}

function SuccessModal({data,onClose}){return <div className="modal-backdrop"><div className="figma-success" data-node-id="29:60"><button className="modal-close" onClick={onClose}><X/></button><div className="success-icon"><Check/></div><h2>Đặt Lịch Thành Công!</h2><p>Cảm ơn quý khách <b>{data.patientName}</b> đã tin tưởng lựa chọn dịch vụ của Bệnh viện Bạch Mai.</p><div className="success-detail"><dl><div><dt>Mã lịch hẹn:</dt><dd>{data.code}</dd></div><div><dt>Ngày & giờ khám:</dt><dd>{fmt(data.appointmentDate)} · {data.appointmentTime.slice(0,5)}</dd></div><div><dt>Chuyên khoa:</dt><dd>{data.specialtyName}</dd></div><div><dt>Bác sĩ phụ trách:</dt><dd>{data.doctorName}</dd></div><div><dt>Cơ sở:</dt><dd>{data.facilityName}</dd></div></dl><QRCodeSVG value={data.qrPayload} size={112}/></div><div className="figma-warning"><b>Lưu ý quan trọng:</b> Vui lòng đến trước giờ hẹn 15 phút và xuất trình mã QR tại quầy tiếp đón.</div><Link className="figma-primary" to="/tra-cuu">Tra cứu lịch hẹn</Link></div></div>}
function addHour(t){const [h,m]=t.split(':').map(Number);return `${String((h+1)%24).padStart(2,'0')}:${String(m).padStart(2,'0')}`}
function fmt(s){return new Date(s+'T00:00:00').toLocaleDateString('vi-VN')}
