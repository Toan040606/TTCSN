# Hệ thống đặt lịch khám trực tuyến — Bệnh viện Bạch Mai

Ứng dụng bài tập lớn gồm frontend ReactJS và backend Spring Boot, được xây dựng theo tài liệu yêu cầu trong `BTL_Nhom14_20261IT6040004.md` và giao diện Figma `TTCSN`.

## Chức năng chính

- Bệnh nhân xem cơ sở, chuyên khoa, bác sĩ và khung giờ còn trống.
- Đặt lịch khám, nhận mã hẹn và QR Code.
- Tra cứu lịch sử bằng số điện thoại + OTP mô phỏng; xem bệnh án và hủy lịch hợp lệ.
- Lễ tân tìm lịch hẹn, xác nhận check-in và cấp số thứ tự.
- Bác sĩ xem hàng đợi, ghi triệu chứng, chẩn đoán, đơn thuốc và hoàn tất lượt khám.
- Quản trị viên xem thống kê, tạo/khóa tài khoản, phân công và đóng/mở ca trực.
- JWT và phân quyền cho `ADMIN`, `RECEPTIONIST`, `DOCTOR`.

## Công nghệ

- Frontend: ReactJS, Vite, React Router, Lucide React, QRCode React.
- Backend: Java 21, Spring Boot 3.5, Spring Security, Spring Data JPA, JWT.
- Cơ sở dữ liệu mặc định: H2 lưu file; hỗ trợ MySQL qua biến môi trường.

## Yêu cầu môi trường

- Java 21 trở lên.
- Node.js 20 trở lên và npm.
- Không bắt buộc cài Maven: dự án có script `backend/mvnw` tự chuẩn bị Maven.

## Chạy dự án

Mở terminal thứ nhất:

```bash
cd backend
./mvnw spring-boot:run
```

Backend chạy tại `http://localhost:8080`. Lần chạy đầu sẽ tự tạo dữ liệu mẫu và database trong `backend/data/`.

Mở terminal thứ hai:

```bash
cd frontend
npm install
npm run dev
```

Truy cập `http://localhost:5173`.

## Tài khoản mẫu

| Vai trò | Tên đăng nhập | Mật khẩu |
|---|---|---|
| Quản trị viên | `admin` | `BachMai@123` |
| Nhân viên lễ tân | `letan` | `BachMai@123` |
| Bác sĩ | `bacsi.an` | `BachMai@123` |

OTP được hiển thị ngay trên giao diện tra cứu trong môi trường demo.

## Cấu hình MySQL tùy chọn

Thiết lập các biến môi trường trước khi chạy backend:

```bash
export DB_URL='jdbc:mysql://localhost:3306/bachmai?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8'
export DB_USERNAME='root'
export DB_PASSWORD='your_password'
export DB_DRIVER='com.mysql.cj.jdbc.Driver'
export JWT_SECRET='replace-with-a-long-random-secret'
export CORS_ORIGIN='http://localhost:5173'
```

Frontend có thể trỏ đến backend khác bằng `VITE_API_URL`.

## Kiểm thử và đóng gói

```bash
cd backend
./mvnw test

cd ../frontend
npm run build
```

Các nhóm API chính:

- `/api/public/**`: danh mục, khung giờ, đặt lịch, OTP, tra cứu và hủy lịch.
- `/api/auth/login`: đăng nhập cán bộ.
- `/api/staff/**`: tiếp đón, hàng đợi và bệnh án.
- `/api/admin/**`: thống kê, tài khoản và lịch trực.

## Cấu trúc và tài liệu

- `frontend/`: mã nguồn ReactJS.
- `backend/`: mã nguồn Spring Boot.
- `PLAN.md`: kế hoạch và kiến trúc triển khai.
- `PROGRESS.md`: nhật ký tiến độ được cập nhật sau mỗi phần hoàn thành.
- `BTL_Nhom14_20261IT6040004.md`: tài liệu yêu cầu gốc.
