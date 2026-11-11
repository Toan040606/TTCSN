package vn.edu.haui.bachmai.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.haui.bachmai.model.*;
import vn.edu.haui.bachmai.repository.*;
import java.time.*;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserAccountRepository users;
    private final FacilityRepository facilities;
    private final SpecialtyRepository specialties;
    private final DoctorRepository doctors;
    private final WorkScheduleRepository schedules;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (users.count() > 0) return;
        var admin = users.save(account("admin", "Quản trị hệ thống", Role.ADMIN));
        users.save(account("letan", "Nguyễn Thu Hà", Role.RECEPTIONIST));
        var doctorAccount1 = users.save(account("bacsi.an", "BS.CKII Nguyễn Minh An", Role.DOCTOR));
        var doctorAccount2 = users.save(account("bacsi.lan", "PGS.TS Trần Ngọc Lan", Role.DOCTOR));
        var doctorAccount3 = users.save(account("bacsi.hung", "ThS.BS Lê Mạnh Hùng", Role.DOCTOR));

        Facility cs1 = facilities.save(Facility.builder().code("CS1").name("Cơ sở 1 — Hà Nội")
            .address("78 Giải Phóng, Phương Mai, Đống Đa, Hà Nội").phone("1900 888 866").build());
        Facility cs2 = facilities.save(Facility.builder().code("CS2").name("Cơ sở 2 — Hà Nam")
            .address("Phường Liêm Tuyền, TP. Phủ Lý, Hà Nam").phone("1900 888 866").build());
        Specialty tim = specialties.save(Specialty.builder().code("TIM").name("Tim mạch").icon("heart-pulse")
            .description("Khám, chẩn đoán và điều trị các bệnh lý tim mạch.").build());
        Specialty thanKinh = specialties.save(Specialty.builder().code("TK").name("Thần kinh").icon("brain")
            .description("Chăm sóc toàn diện các bệnh lý thần kinh và đột quỵ.").build());
        Specialty tieuHoa = specialties.save(Specialty.builder().code("TH").name("Tiêu hóa").icon("activity")
            .description("Khám và điều trị bệnh lý đường tiêu hóa, gan mật.").build());
        specialties.save(Specialty.builder().code("NOI").name("Nội tổng hợp").icon("stethoscope").description("Khám nội khoa tổng quát.").build());
        specialties.save(Specialty.builder().code("CXK").name("Cơ xương khớp").icon("bone").description("Điều trị bệnh lý cơ, xương và khớp.").build());
        specialties.save(Specialty.builder().code("NHI").name("Nhi khoa").icon("baby").description("Khám và chăm sóc sức khỏe trẻ em.").build());

        Doctor d1 = doctors.save(Doctor.builder().code("BS001").fullName("Nguyễn Minh An").title("BS.CKII")
            .bio("Hơn 15 năm kinh nghiệm điều trị tim mạch can thiệp.").specialty(tim).facility(cs1).account(doctorAccount1).active(true).build());
        Doctor d2 = doctors.save(Doctor.builder().code("BS002").fullName("Trần Ngọc Lan").title("PGS.TS")
            .bio("Chuyên gia đầu ngành về thần kinh và đột quỵ.").specialty(thanKinh).facility(cs1).account(doctorAccount2).active(true).build());
        Doctor d3 = doctors.save(Doctor.builder().code("BS003").fullName("Lê Mạnh Hùng").title("ThS.BS")
            .bio("Chuyên sâu nội soi và bệnh lý tiêu hóa.").specialty(tieuHoa).facility(cs2).account(doctorAccount3).active(true).build());

        for (int day = 0; day < 30; day++) {
            LocalDate date = LocalDate.now().plusDays(day);
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) continue;
            schedules.saveAll(List.of(schedule(d1, cs1, date, Shift.MORNING, "P.201"), schedule(d1, cs1, date, Shift.AFTERNOON, "P.201"),
                schedule(d2, cs1, date, Shift.MORNING, "P.305"), schedule(d3, cs2, date, Shift.AFTERNOON, "P.108")));
        }
    }

    private UserAccount account(String username, String name, Role role) {
        return UserAccount.builder().username(username).password(encoder.encode("BachMai@123")).fullName(name).role(role).enabled(true).build();
    }
    private WorkSchedule schedule(Doctor d, Facility f, LocalDate date, Shift shift, String room) {
        return WorkSchedule.builder().doctor(d).facility(f).workDate(date).shift(shift).capacity(18).open(true).room(room).build();
    }
}
