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
import java.time.*;
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
    private final SpecialtyRepository specialties;
    private final NotificationLogRepository notifications;
    private final PasswordEncoder encoder;

    @GetMapping("/stats")
    public DashboardStats stats() {
        return new DashboardStats(appointments.count(), appointments.countByStatus(AppointmentStatus.PENDING) + appointments.countByStatus(AppointmentStatus.CONFIRMED),
            appointments.countByStatus(AppointmentStatus.CHECKED_IN), appointments.countByStatus(AppointmentStatus.COMPLETED),
            appointments.countByStatus(AppointmentStatus.CANCELLED), doctors.count(), patients.count());
    }

    @GetMapping("/notifications")
    public List<NotificationView> notifications() {
        return notifications.findTop100ByOrderByCreatedAtDesc().stream().map(n -> new NotificationView(n.getId(),
            n.getAppointment().getCode(), n.getChannel(), n.getType(), n.getRecipient(), n.getContent(), n.getStatus(),
            n.getCreatedAt(), n.getSentAt())).toList();
    }

    @GetMapping("/reports/by-facility")
    public List<ReportView> reportByFacility() {
        return facilities.findAll().stream().map(f -> report(f.getName(), appointments.findAll().stream()
            .filter(a -> a.getFacility().getId().equals(f.getId())).toList())).toList();
    }

    @GetMapping("/reports/by-time-slot")
    public List<ReportView> reportByTimeSlot() {
        return appointments.findAll().stream().collect(java.util.stream.Collectors.groupingBy(a -> a.getAppointmentTime().toString()))
            .entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e -> report(e.getKey(), e.getValue())).toList();
    }

    @GetMapping("/users")
    public List<UserView> users() { return users.findAll().stream().map(this::userView).toList(); }

    @PostMapping("/users")
    @Transactional
    public UserView createUser(@Valid @RequestBody UserRequest request) {
        if (users.existsByUsername(request.username())) throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        String rawPassword = request.password() == null || request.password().isBlank() ? "BachMai@123" : request.password();
        Facility facility = request.facilityId() == null ? null : facilities.findById(request.facilityId())
            .orElseThrow(() -> new IllegalArgumentException("Cơ sở không tồn tại"));
        return userView(users.save(UserAccount.builder().username(request.username()).password(encoder.encode(rawPassword))
            .fullName(request.fullName()).phone(request.phone()).role(request.role()).facility(facility).enabled(true).build()));
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

    @PutMapping("/users/{id}")
    @Transactional
    public UserView updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        UserAccount user = users.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        Facility facility = request.facilityId() == null ? null : facilities.findById(request.facilityId())
            .orElseThrow(() -> new IllegalArgumentException("Cơ sở không tồn tại"));
        user.setFullName(request.fullName()); user.setPhone(request.phone()); user.setRole(request.role()); user.setFacility(facility);
        return userView(user);
    }

    @PatchMapping("/users/{id}/reset-password")
    @Transactional
    public void resetPassword(@PathVariable Long id, @Valid @RequestBody PasswordResetRequest request) {
        UserAccount user = users.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        user.setPassword(encoder.encode(request.password()));
    }

    @PostMapping("/specialties")
    public Specialty createSpecialty(@Valid @RequestBody SpecialtyRequest request) {
        return specialties.save(Specialty.builder().code(request.code()).name(request.name())
            .description(request.description()).icon(request.icon()).build());
    }

    @PutMapping("/specialties/{id}")
    @Transactional
    public Specialty updateSpecialty(@PathVariable Long id, @Valid @RequestBody SpecialtyRequest request) {
        Specialty s = specialties.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chuyên khoa"));
        s.setCode(request.code()); s.setName(request.name()); s.setDescription(request.description()); s.setIcon(request.icon());
        return s;
    }

    @PostMapping("/doctors")
    public DoctorView createDoctor(@Valid @RequestBody DoctorRequest request) {
        return doctorView(doctors.save(applyDoctor(new Doctor(), request)));
    }

    @PutMapping("/doctors/{id}")
    @Transactional
    public DoctorView updateDoctor(@PathVariable Long id, @Valid @RequestBody DoctorRequest request) {
        Doctor d = doctors.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ"));
        return doctorView(applyDoctor(d, request));
    }

    @GetMapping("/schedules")
    public List<ScheduleView> schedules() { return schedules.findAll().stream().map(this::scheduleView).toList(); }

    @PostMapping("/schedules")
    public ScheduleView createSchedule(@Valid @RequestBody ScheduleRequest request) {
        Doctor doctor = doctors.findById(request.doctorId()).orElseThrow(() -> new IllegalArgumentException("Bác sĩ không tồn tại"));
        Facility facility = facilities.findById(request.facilityId()).orElseThrow(() -> new IllegalArgumentException("Cơ sở không tồn tại"));
        if (!doctor.getFacility().getId().equals(facility.getId()))
            throw new IllegalArgumentException("Bác sĩ không làm việc tại cơ sở đã chọn");
        return scheduleView(schedules.save(WorkSchedule.builder().doctor(doctor).facility(facility).workDate(request.workDate())
            .shift(request.shift()).capacity(request.capacity()).room(request.room()).open(true).build()));
    }

    @PatchMapping("/schedules/{id}/toggle")
    @Transactional
    public ScheduleView toggleSchedule(@PathVariable Long id) {
        WorkSchedule s = schedules.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca trực"));
        if (s.isOpen() && hasActiveAppointments(s))
            throw new IllegalArgumentException("Không thể đóng ca khi còn lịch hẹn chưa xử lý");
        s.setOpen(!s.isOpen());
        return scheduleView(s);
    }

    @PutMapping("/schedules/{id}")
    @Transactional
    public ScheduleView updateSchedule(@PathVariable Long id, @Valid @RequestBody ScheduleRequest request) {
        WorkSchedule s = schedules.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca trực"));
        if (hasActiveAppointments(s)) throw new IllegalArgumentException("Không thể sửa ca đã có lịch hẹn chưa xử lý");
        Doctor doctor = doctors.findById(request.doctorId()).orElseThrow(() -> new IllegalArgumentException("Bác sĩ không tồn tại"));
        Facility facility = facilities.findById(request.facilityId()).orElseThrow(() -> new IllegalArgumentException("Cơ sở không tồn tại"));
        if (!doctor.getFacility().getId().equals(facility.getId()))
            throw new IllegalArgumentException("Bác sĩ không làm việc tại cơ sở đã chọn");
        s.setDoctor(doctor); s.setFacility(facility); s.setWorkDate(request.workDate()); s.setShift(request.shift());
        s.setCapacity(request.capacity()); s.setRoom(request.room());
        return scheduleView(s);
    }

    private UserView userView(UserAccount u) { return new UserView(u.getId(), u.getUsername(), u.getFullName(), u.getPhone(), u.getRole(), u.isEnabled(),
        u.getFacility() == null ? null : u.getFacility().getId(), u.getFacility() == null ? null : u.getFacility().getName()); }
    private ScheduleView scheduleView(WorkSchedule s) { return new ScheduleView(s.getId(), s.getDoctor().getId(), s.getDoctor().getFullName(),
        s.getDoctor().getSpecialty().getName(), s.getFacility().getName(), s.getWorkDate(), s.getShift(), s.getCapacity(), s.isOpen(), s.getRoom()); }
    private boolean hasActiveAppointments(WorkSchedule s) {
        LocalTime start = s.getShift() == Shift.MORNING ? LocalTime.of(7, 0) : LocalTime.of(13, 30);
        LocalTime end = s.getShift() == Shift.MORNING ? LocalTime.of(11, 30) : LocalTime.of(17, 0);
        return appointments.findByDoctorIdAndAppointmentDateOrderByAppointmentTime(s.getDoctor().getId(), s.getWorkDate()).stream()
            .anyMatch(a -> !a.getAppointmentTime().isBefore(start) && a.getAppointmentTime().isBefore(end) &&
                List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED, AppointmentStatus.CHECKED_IN).contains(a.getStatus()));
    }
    private ReportView report(String key, List<Appointment> data) {
        return new ReportView(key, data.size(), data.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count(),
            data.stream().filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count());
    }
    private Doctor applyDoctor(Doctor d, DoctorRequest r) {
        Specialty specialty = specialties.findById(r.specialtyId()).orElseThrow(() -> new IllegalArgumentException("Chuyên khoa không tồn tại"));
        Facility facility = facilities.findById(r.facilityId()).orElseThrow(() -> new IllegalArgumentException("Cơ sở không tồn tại"));
        UserAccount account = r.accountId() == null ? null : users.findById(r.accountId()).orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại"));
        if (account != null && account.getRole() != Role.DOCTOR) throw new IllegalArgumentException("Tài khoản phải có vai trò bác sĩ");
        d.setCode(r.code()); d.setFullName(r.fullName()); d.setTitle(r.title()); d.setBio(r.bio()); d.setAvatarUrl(r.avatarUrl());
        d.setSpecialty(specialty); d.setFacility(facility); d.setAccount(account); d.setActive(r.active());
        return d;
    }
    private DoctorView doctorView(Doctor d) {
        return new DoctorView(d.getId(), d.getCode(), d.getFullName(), d.getTitle(), d.getBio(), d.getAvatarUrl(),
            d.getSpecialty().getId(), d.getSpecialty().getName(), d.getFacility().getId(), d.getFacility().getName());
    }
}
