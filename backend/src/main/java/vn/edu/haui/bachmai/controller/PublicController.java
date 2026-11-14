package vn.edu.haui.bachmai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.edu.haui.bachmai.dto.ApiDtos.*;
import vn.edu.haui.bachmai.model.*;
import vn.edu.haui.bachmai.repository.*;
import vn.edu.haui.bachmai.service.AppointmentService;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {
    private final FacilityRepository facilities;
    private final SpecialtyRepository specialties;
    private final DoctorRepository doctors;
    private final AppointmentService appointmentService;

    @GetMapping("/facilities") public List<Facility> facilities() { return facilities.findAll(); }
    @GetMapping("/specialties") public List<Specialty> specialties() { return specialties.findAll(); }
    @GetMapping("/health") public Map<String, Object> health() {
        return Map.of("status", "UP", "time", LocalDateTime.now(), "database", facilities.count() >= 0 ? "UP" : "DOWN");
    }

    @GetMapping("/doctors")
    public List<DoctorView> doctors(@RequestParam(required = false) Long specialtyId, @RequestParam(required = false) Long facilityId) {
        List<Doctor> result = specialtyId != null && facilityId != null
            ? doctors.findBySpecialtyIdAndFacilityIdAndActiveTrue(specialtyId, facilityId) : doctors.findByActiveTrue();
        return result.stream().map(this::doctorView).toList();
    }

    @GetMapping("/doctors/{doctorId}/slots")
    public List<LocalTime> slots(@PathVariable Long doctorId, @RequestParam LocalDate date) {
        return appointmentService.availableTimes(doctorId, date);
    }

    @PostMapping("/appointments")
    public AppointmentView create(@Valid @RequestBody AppointmentRequest request) { return appointmentService.create(request); }

    @PostMapping("/lookup/request-otp")
    public OtpResponse requestOtp(@Valid @RequestBody OtpRequest request) { return appointmentService.requestOtp(request.phone()); }

    @PostMapping("/lookup/verify-otp")
    public OtpVerifyResponse verifyOtp(@Valid @RequestBody OtpVerifyRequest request) { return appointmentService.verifyOtp(request); }

    @PostMapping("/appointments/{code}/cancel")
    public AppointmentView cancel(@PathVariable String code, @Valid @RequestBody CancelRequest request) {
        return appointmentService.cancel(code, request.lookupToken());
    }

    @PutMapping("/appointments/{code}/reschedule")
    public AppointmentView reschedule(@PathVariable String code, @Valid @RequestBody RescheduleRequest request) {
        return appointmentService.reschedule(code, request);
    }

    private DoctorView doctorView(Doctor d) {
        return new DoctorView(d.getId(), d.getCode(), d.getFullName(), d.getTitle(), d.getBio(), d.getAvatarUrl(),
            d.getSpecialty().getId(), d.getSpecialty().getName(), d.getFacility().getId(), d.getFacility().getName());
    }
}
