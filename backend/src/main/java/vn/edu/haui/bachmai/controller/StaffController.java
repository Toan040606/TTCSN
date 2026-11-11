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
    private final MedicalRecordRepository records;
    private final AppointmentService appointmentService;

    @GetMapping("/appointments/today")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public List<AppointmentView> today() {
        return appointments.findByAppointmentDateOrderByAppointmentTime(LocalDate.now()).stream().map(appointmentService::view).toList();
    }

    @GetMapping("/appointments/search")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public List<AppointmentView> search(@RequestParam String query) {
        Optional<Appointment> byCode = appointments.findByCode(query.trim().toUpperCase());
        if (byCode.isPresent()) return List.of(appointmentService.view(byCode.get()));
        return appointments.findByPatientPhoneOrderByCreatedAtDesc(query.trim()).stream().map(appointmentService::view).toList();
    }

    @PatchMapping("/appointments/{id}/check-in")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Transactional
    public AppointmentView checkIn(@PathVariable Long id) {
        Appointment a = appointments.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn"));
        if (a.getStatus() != AppointmentStatus.CONFIRMED && a.getStatus() != AppointmentStatus.PENDING)
            throw new IllegalArgumentException("Lịch hẹn không thể check-in ở trạng thái hiện tại");
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
        MedicalRecord record = records.findByAppointmentId(id).orElseGet(() -> MedicalRecord.builder().appointment(a).build());
        record.setSymptoms(request.symptoms());
        record.setDiagnosis(request.diagnosis());
        record.setPrescription(request.prescription());
        record.setNotes(request.notes());
        record.setUpdatedAt(LocalDateTime.now());
        records.save(record);
        a.setStatus(AppointmentStatus.COMPLETED);
        return appointmentService.view(a);
    }
}
