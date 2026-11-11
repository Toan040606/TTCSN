package vn.edu.haui.bachmai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.edu.haui.bachmai.dto.ApiDtos.*;
import vn.edu.haui.bachmai.model.*;
import vn.edu.haui.bachmai.repository.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final UserAccountRepository users;
    private final DoctorRepository doctors;
    private final PatientRepository patients;
    private final AppointmentRepository appointments;
    private final WorkScheduleRepository schedules;
    private final FacilityRepository facilities;
    private final PasswordEncoder encoder;

    @GetMapping("/stats")
    public DashboardStats stats() {
        return new DashboardStats(appointments.count(), appointments.countByStatus(AppointmentStatus.PENDING) + appointments.countByStatus(AppointmentStatus.CONFIRMED),
            appointments.countByStatus(AppointmentStatus.CHECKED_IN), appointments.countByStatus(AppointmentStatus.COMPLETED),
            appointments.countByStatus(AppointmentStatus.CANCELLED), doctors.count(), patients.count());
    }

    @GetMapping("/users")
    public List<UserView> users() { return users.findAll().stream().map(this::userView).toList(); }

    @PostMapping("/users")
    @Transactional
    public UserView createUser(@Valid @RequestBody UserRequest request) {
        if (users.existsByUsername(request.username())) throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        String rawPassword = request.password() == null || request.password().isBlank() ? "BachMai@123" : request.password();
        return userView(users.save(UserAccount.builder().username(request.username()).password(encoder.encode(rawPassword))
            .fullName(request.fullName()).phone(request.phone()).role(request.role()).enabled(true).build()));
    }

    @PatchMapping("/users/{id}/toggle")
    @Transactional
    public UserView toggle(@PathVariable Long id) {
        UserAccount user = users.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        if (user.getRole() == Role.ADMIN && user.isEnabled() && users.findAll().stream().filter(u -> u.getRole() == Role.ADMIN && u.isEnabled()).count() <= 1)
            throw new IllegalArgumentException("Không thể khóa quản trị viên cuối cùng");
        user.setEnabled(!user.isEnabled());
        return userView(user);
    }

    @GetMapping("/schedules")
    public List<ScheduleView> schedules() { return schedules.findAll().stream().map(this::scheduleView).toList(); }

    @PostMapping("/schedules")
    public ScheduleView createSchedule(@Valid @RequestBody ScheduleRequest request) {
        Doctor doctor = doctors.findById(request.doctorId()).orElseThrow(() -> new IllegalArgumentException("Bác sĩ không tồn tại"));
        Facility facility = facilities.findById(request.facilityId()).orElseThrow(() -> new IllegalArgumentException("Cơ sở không tồn tại"));
        return scheduleView(schedules.save(WorkSchedule.builder().doctor(doctor).facility(facility).workDate(request.workDate())
            .shift(request.shift()).capacity(request.capacity()).room(request.room()).open(true).build()));
    }

    @PatchMapping("/schedules/{id}/toggle")
    @Transactional
    public ScheduleView toggleSchedule(@PathVariable Long id) {
        WorkSchedule s = schedules.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca trực"));
        s.setOpen(!s.isOpen());
        return scheduleView(s);
    }

    private UserView userView(UserAccount u) { return new UserView(u.getId(), u.getUsername(), u.getFullName(), u.getPhone(), u.getRole(), u.isEnabled()); }
    private ScheduleView scheduleView(WorkSchedule s) { return new ScheduleView(s.getId(), s.getDoctor().getId(), s.getDoctor().getFullName(),
        s.getDoctor().getSpecialty().getName(), s.getFacility().getName(), s.getWorkDate(), s.getShift(), s.getCapacity(), s.isOpen(), s.getRoom()); }
}
