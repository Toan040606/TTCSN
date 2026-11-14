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
- Không bắt buộc cài Maven: dùng `backend/mvnw.cmd` để tự chuẩn bị Maven trên Windows.

## Chạy dự án trên Windows

### Cài Node.js và npm

`npm` được cài kèm theo Node.js, không cần tải riêng. Cài Node.js 20 trở lên từ trang chính thức `https://nodejs.org`, giữ các tùy chọn mặc định trong trình cài đặt, sau đó đóng và mở lại PowerShell/Windows Terminal.

Kiểm tra Node.js và npm:

```powershell
node --version
npm --version
```

Nếu cả hai lệnh đều in ra phiên bản thì môi trường frontend đã sẵn sàng. Nếu Windows báo không nhận diện được `node` hoặc `npm`, hãy mở terminal mới; nếu vẫn lỗi thì kiểm tra Node.js đã được thêm vào biến môi trường `PATH`.

Nếu PowerShell báo `npm.ps1 cannot be loaded because running scripts is disabled`, có thể dùng trực tiếp `npm.cmd` mà không cần thay đổi chính sách bảo mật:

```powershell
npm.cmd install
npm.cmd run dev
```

### Kiểm tra Java

Backend yêu cầu Java 21 trở lên:

```powershell
java -version
```

Sau khi clone dự án, mở PowerShell hoặc Windows Terminal tại thư mục gốc và thực hiện các bước dưới đây.

Mở terminal thứ nhất để chạy backend:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Lần chạy đầu, `mvnw.cmd` sẽ tự tải Maven 3.9.11 vào `backend\.mvn`; không cần cài Maven toàn hệ thống. Backend chạy tại `http://localhost:8080` và tự tạo database H2 trong `backend\data`.

Mở terminal thứ hai để chạy frontend:

```powershell
cd frontend
npm install
npm run dev
```

`npm install` đọc `package.json` và cài dependency vào `frontend\node_modules`. Thư mục này được Git bỏ qua và không cần commit.

Truy cập `http://localhost:5173`.

Nếu dùng Command Prompt (`cmd.exe`), các lệnh tương ứng là:

```bat
cd backend
mvnw.cmd spring-boot:run
```

```bat
cd frontend
npm install
npm run dev
```

Nếu PowerShell hỏi quyền truy cập mạng trong lần đầu chạy, cho phép Java và Node.js truy cập mạng riêng để frontend kết nối được với backend.

## Tài khoản mẫu

| Vai trò | Tên đăng nhập | Mật khẩu |
|---|---|---|
| Quản trị viên | `admin` | `BachMai@123` |
| Nhân viên lễ tân | `letan` | `BachMai@123` |
| Bác sĩ | `bacsi.an` | `BachMai@123` |

OTP được hiển thị ngay trên giao diện tra cứu trong môi trường demo.

## Cấu hình MySQL tùy chọn

Mặc định dự án dùng H2 nên không cần cài database. Để chuyển sang MySQL trên Windows PowerShell, thiết lập biến môi trường trong chính terminal sẽ chạy backend:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/bachmai?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8'
$env:DB_USERNAME = 'root'
$env:DB_PASSWORD = 'your_password'
$env:DB_DRIVER = 'com.mysql.cj.jdbc.Driver'
$env:JWT_SECRET = 'replace-with-a-long-random-secret'
$env:CORS_ORIGIN = 'http://localhost:5173'
.\backend\mvnw.cmd -f .\backend\pom.xml spring-boot:run
```

Trên Windows Command Prompt:

```bat
set "DB_URL=jdbc:mysql://localhost:3306/bachmai?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8"
set "DB_USERNAME=root"
set "DB_PASSWORD=your_password"
set "DB_DRIVER=com.mysql.cj.jdbc.Driver"
set "JWT_SECRET=replace-with-a-long-random-secret"
set "CORS_ORIGIN=http://localhost:5173"
backend\mvnw.cmd -f backend\pom.xml spring-boot:run
```

Frontend có thể trỏ đến backend khác bằng `VITE_API_URL`.

## Kiểm thử và đóng gói

Windows PowerShell:

```powershell
cd backend
.\mvnw.cmd test

cd ..\frontend
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
