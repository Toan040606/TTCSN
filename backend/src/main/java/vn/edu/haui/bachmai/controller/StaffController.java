package vn.edu.haui.bachmai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.edu.haui.bachmai.dto.ApiDtos.*;
import vn.edu.haui.bachmai.model.*;
import vn.edu.haui.bachmai.repository.*;
import vn.edu.haui.bachmai.service.AppointmentService;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {
    private final AppointmentRepository appointments;
    private final DoctorRepository doctors;
    private final UserAccountRepository users;
    private final WorkScheduleRepository schedules;
    private final MedicalRecordRepository records;
    private final PrescriptionItemRepository prescriptionItems;
    private final AppointmentService appointmentService;

    @GetMapping("/appointments/today")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public List<AppointmentView> today(Authentication auth) {
        UserAccount user = users.findByUsername(auth.getName()).orElseThrow();
        List<Appointment> result = user.getRole() == Role.ADMIN || user.getFacility() == null
            ? appointments.findByAppointmentDateOrderByAppointmentTime(LocalDate.now())
            : appointments.findByFacilityIdAndAppointmentDateOrderByAppointmentTime(user.getFacility().getId(), LocalDate.now());
        return result.stream().map(appointmentService::view).toList();
    }

    @GetMapping("/appointments/search")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public List<AppointmentView> search(Authentication auth, @RequestParam String query) {
        UserAccount user = users.findByUsername(auth.getName()).orElseThrow();
        Optional<Appointment> byCode = appointments.findByCode(query.trim().toUpperCase());
        if (byCode.isPresent()) {
            ensureFacility(user, byCode.get());
            return List.of(appointmentService.view(byCode.get()));
        }
        List<Appointment> result = user.getRole() == Role.ADMIN || user.getFacility() == null
            ? appointments.findByPatientPhoneOrderByCreatedAtDesc(query.trim())
            : appointments.findByFacilityIdAndPatientPhoneOrderByCreatedAtDesc(user.getFacility().getId(), query.trim());
        return result.stream().map(appointmentService::view).toList();
    }

    @PostMapping("/appointments")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public AppointmentView createAtCounter(Authentication auth, @Valid @RequestBody AppointmentRequest request) {
        UserAccount user = users.findByUsername(auth.getName()).orElseThrow();
        if (user.getRole() != Role.ADMIN && user.getFacility() != null && !user.getFacility().getId().equals(request.facilityId()))
            throw new IllegalArgumentException("Bạn chỉ được đặt lịch tại cơ sở làm việc của mình");
        return appointmentService.create(request);
    }

    @PostMapping("/appointments/scan-qr")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Transactional
    public AppointmentView scanQr(Authentication auth, @RequestBody Map<String, String> payload) {
        String qr = Optional.ofNullable(payload.get("qrPayload")).orElse("").trim();
        Appointment a = appointments.findByQrPayload(qr).orElseGet(() -> {
            String code = qr.startsWith("BACHMAI:") ? qr.substring("BACHMAI:".length()) : qr;
            return appointments.findByCode(code.toUpperCase()).orElseThrow(() -> new IllegalArgumentException("Mã QR không hợp lệ"));
        });
        return checkIn(auth, a.getId());
    }

    @PatchMapping("/appointments/{id}/check-in")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Transactional
    public AppointmentView checkIn(Authentication auth, @PathVariable Long id) {
        Appointment a = appointments.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn"));
        UserAccount user = users.findByUsername(auth.getName()).orElseThrow();
        ensureFacility(user, a);
        if (!a.getAppointmentDate().equals(LocalDate.now()))
            throw new IllegalArgumentException("Chỉ có thể check-in lịch hẹn trong ngày hôm nay");
        if (a.getStatus() != AppointmentStatus.CONFIRMED && a.getStatus() != AppointmentStatus.PENDING)
            throw new IllegalArgumentException("Lịch hẹn không thể check-in ở trạng thái hiện tại");
        Shift shift = a.getAppointmentTime().isBefore(LocalTime.NOON) ? Shift.MORNING : Shift.AFTERNOON;
        schedules.lockOpenSchedule(a.getDoctor().getId(), a.getAppointmentDate(), shift)
            .orElseThrow(() -> new IllegalArgumentException("Ca khám đã đóng hoặc không còn hiệu lực"));
        a.setStatus(AppointmentStatus.CHECKED_IN);
        a.setCheckedInAt(LocalDateTime.now());
        a.setQueueNumber(appointments.findByDoctorIdAndAppointmentDateOrderByAppointmentTime(a.getDoctor().getId(), a.getAppointmentDate()).stream()
            .map(Appointment::getQueueNumber).filter(Objects::nonNull).max(Integer::compareTo).orElse(0) + 1);
        return appointmentService.view(a);
    }

    @GetMapping("/doctor/appointments")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<AppointmentView> doctorQueue(Authentication auth, @RequestParam(required = false) LocalDate date) {
        Doctor doctor = doctors.findByAccountUsername(auth.getName()).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ bác sĩ"));
        return appointments.findByDoctorIdAndAppointmentDateOrderByAppointmentTime(doctor.getId(), date == null ? LocalDate.now() : date)
            .stream().map(appointmentService::view).toList();
    }

    @PutMapping("/doctor/appointments/{id}/record")
    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    public AppointmentView updateRecord(Authentication auth, @PathVariable Long id, @Valid @RequestBody MedicalRecordRequest request) {
        Doctor doctor = doctors.findByAccountUsername(auth.getName()).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ bác sĩ"));
        Appointment a = appointments.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn"));
        if (!a.getDoctor().getId().equals(doctor.getId())) throw new IllegalArgumentException("Bạn không phụ trách lịch hẹn này");
        if (a.getStatus() != AppointmentStatus.CHECKED_IN && a.getStatus() != AppointmentStatus.COMPLETED)
            throw new IllegalArgumentException("Chỉ được cập nhật bệnh án sau khi bệnh nhân đã check-in");
        MedicalRecord record = records.findByAppointmentId(id).orElseGet(() -> MedicalRecord.builder().appointment(a).build());
        record.setSymptoms(request.symptoms());
        record.setDiagnosis(request.diagnosis());
        record.setPrescription(request.prescription());
        record.setNotes(request.notes());
        record.setUpdatedAt(LocalDateTime.now());
        records.save(record);
        prescriptionItems.deleteByMedicalRecordId(record.getId());
        if (request.medications() != null) request.medications().stream()
            .filter(i -> i.name() != null && !i.name().isBlank())
            .forEach(i -> prescriptionItems.save(PrescriptionItem.builder().medicalRecord(record).name(i.name())
                .dosage(i.dosage()).frequency(i.frequency()).days(i.days()).instruction(i.instruction()).build()));
        a.setStatus(AppointmentStatus.COMPLETED);
        return appointmentService.view(a);
    }

    @GetMapping("/doctor/patients/{phone}/history")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<AppointmentView> patientHistory(Authentication auth, @PathVariable String phone) {
        Doctor doctor = doctors.findByAccountUsername(auth.getName()).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ bác sĩ"));
        List<Appointment> history = appointments.findByPatientPhoneOrderByCreatedAtDesc(phone);
        if (history.stream().noneMatch(a -> a.getDoctor().getId().equals(doctor.getId())))
            throw new IllegalArgumentException("Bạn chưa từng phụ trách bệnh nhân này");
        return history.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).map(appointmentService::view).toList();
    }

    private void ensureFacility(UserAccount user, Appointment appointment) {
        if (user.getRole() != Role.ADMIN && user.getFacility() != null &&
            !user.getFacility().getId().equals(appointment.getFacility().getId()))
            throw new IllegalArgumentException("Lịch hẹn không thuộc cơ sở làm việc của bạn");
    }
}
