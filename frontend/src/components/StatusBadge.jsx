const labels = {PENDING:'Chờ xác nhận', CONFIRMED:'Đã xác nhận', CHECKED_IN:'Đã check-in', IN_PROGRESS:'Đang khám', COMPLETED:'Hoàn thành', CANCELLED:'Đã hủy'}
export default function StatusBadge({ status }) { return <span className={`status status-${status?.toLowerCase()}`}>{labels[status] || status}</span> }
