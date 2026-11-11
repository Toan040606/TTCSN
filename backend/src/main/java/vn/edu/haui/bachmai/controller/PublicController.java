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

    @GetMapping("/doctors")
    public List<Doctor> doctors(@RequestParam(required = false) Long specialtyId, @RequestParam(required = false) Long facilityId) {
        if (specialtyId != null && facilityId != null) return doctors.findBySpecialtyIdAndFacilityIdAndActiveTrue(specialtyId, facilityId);
        return doctors.findByActiveTrue();
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
}
