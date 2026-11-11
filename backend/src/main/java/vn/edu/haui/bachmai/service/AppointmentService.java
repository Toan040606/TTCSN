package vn.edu.haui.bachmai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.haui.bachmai.dto.ApiDtos.*;
import vn.edu.haui.bachmai.model.*;
import vn.edu.haui.bachmai.repository.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointments;
    private final PatientRepository patients;
    private final FacilityRepository facilities;
    private final SpecialtyRepository specialties;
    private final DoctorRepository doctors;
    private final WorkScheduleRepository schedules;
    private final MedicalRecordRepository records;
    private final Map<String, OtpSession> otpSessions = new ConcurrentHashMap<>();
    private final Map<String, LookupSession> lookupSessions = new ConcurrentHashMap<>();

    @Transactional
    public AppointmentView create(AppointmentRequest r) {
        var facility = facilities.findById(r.facilityId()).orElseThrow(() -> new IllegalArgumentException("Cơ sở khám không tồn tại"));
        var specialty = specialties.findById(r.specialtyId()).orElseThrow(() -> new IllegalArgumentException("Chuyên khoa không tồn tại"));
        var doctor = doctors.findById(r.doctorId()).orElseThrow(() -> new IllegalArgumentException("Bác sĩ không tồn tại"));
        if (!doctor.isActive() || !doctor.getSpecialty().getId().equals(specialty.getId()))
            throw new IllegalArgumentException("Bác sĩ không thuộc chuyên khoa đã chọn");
        if (!doctor.getFacility().getId().equals(facility.getId()))
            throw new IllegalArgumentException("Bác sĩ không làm việc tại cơ sở đã chọn");
        if (!availableTimes(doctor.getId(), r.appointmentDate()).contains(r.appointmentTime()))
            throw new IllegalArgumentException("Khung giờ đã hết chỗ hoặc không thuộc lịch làm việc của bác sĩ");

        Patient patient = Patient.builder().fullName(r.fullName()).phone(normalizePhone(r.phone())).email(r.email())
            .dateOfBirth(r.dateOfBirth()).gender(r.gender()).address(r.address()).build();
        patients.save(patient);
        String code = "BM" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + String.format("%04d", new Random().nextInt(10000));
        while (appointments.findByCode(code).isPresent()) code = code.substring(0, code.length() - 4) + String.format("%04d", new Random().nextInt(10000));
        Appointment appointment = Appointment.builder().code(code).patient(patient).facility(facility).specialty(specialty)
            .doctor(doctor).appointmentDate(r.appointmentDate()).appointmentTime(r.appointmentTime()).reason(r.reason())
            .status(AppointmentStatus.CONFIRMED).qrPayload("BACHMAI:" + code).build();
        return view(appointments.save(appointment));
    }

    public List<LocalTime> availableTimes(Long doctorId, LocalDate date) {
        if (date.isBefore(LocalDate.now())) return List.of();
        List<LocalTime> result = new ArrayList<>();
        for (WorkSchedule schedule : schedules.findByDoctorIdAndWorkDateAndOpenTrue(doctorId, date)) {
            LocalTime start = schedule.getShift() == Shift.MORNING ? LocalTime.of(7, 0) : LocalTime.of(13, 30);
            LocalTime end = schedule.getShift() == Shift.MORNING ? LocalTime.of(11, 30) : LocalTime.of(17, 0);
            int slotCapacity = Math.max(1, (int) Math.ceil(schedule.getCapacity() / (double) ((int) (Duration.between(start, end).toMinutes() / 30))));
            for (LocalTime time = start; time.isBefore(end); time = time.plusMinutes(30)) {
                long used = appointments.countByDoctorIdAndAppointmentDateAndAppointmentTimeBetweenAndStatusNot(
                    doctorId, date, time, time, AppointmentStatus.CANCELLED);
                if (used < slotCapacity && !(date.equals(LocalDate.now()) && time.isBefore(LocalTime.now()))) result.add(time);
            }
        }
        return result;
    }

    public OtpResponse requestOtp(String rawPhone) {
        String phone = normalizePhone(rawPhone);
        if (appointments.findByPatientPhoneOrderByCreatedAtDesc(phone).isEmpty())
            throw new IllegalArgumentException("Số điện thoại không tìm thấy trong hệ thống");
        String requestId = UUID.randomUUID().toString();
        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        otpSessions.put(requestId, new OtpSession(phone, otp, Instant.now().plusSeconds(300)));
        return new OtpResponse(requestId, "Mã OTP có hiệu lực trong 5 phút", otp);
    }

    public OtpVerifyResponse verifyOtp(OtpVerifyRequest request) {
        OtpSession session = otpSessions.remove(request.requestId());
        if (session == null || session.expiresAt().isBefore(Instant.now()) || !session.otp().equals(request.otp()))
            throw new IllegalArgumentException("Mã OTP không chính xác hoặc đã hết hiệu lực");
        String token = UUID.randomUUID().toString();
        lookupSessions.put(token, new LookupSession(session.phone(), Instant.now().plusSeconds(1800)));
        return new OtpVerifyResponse(token, appointments.findByPatientPhoneOrderByCreatedAtDesc(session.phone()).stream().map(this::view).toList());
    }

    @Transactional
    public AppointmentView cancel(String code, String lookupToken) {
        LookupSession session = lookupSessions.get(lookupToken);
        Appointment appointment = appointments.findByCode(code).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn"));
        if (session == null || session.expiresAt().isBefore(Instant.now()) || !session.phone().equals(appointment.getPatient().getPhone()))
            throw new IllegalArgumentException("Phiên tra cứu không hợp lệ hoặc đã hết hạn");
        if (!List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED).contains(appointment.getStatus()))
            throw new IllegalArgumentException("Lịch hẹn ở trạng thái hiện tại không thể hủy");
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return view(appointment);
    }

    public AppointmentView view(Appointment a) {
        MedicalRecordView record = records.findByAppointmentId(a.getId())
            .map(r -> new MedicalRecordView(r.getSymptoms(), r.getDiagnosis(), r.getPrescription(), r.getNotes(), r.getUpdatedAt())).orElse(null);
        return new AppointmentView(a.getId(), a.getCode(), a.getPatient().getFullName(), a.getPatient().getPhone(),
            a.getPatient().getEmail(), a.getFacility().getName(), a.getSpecialty().getName(), a.getDoctor().getId(),
            a.getDoctor().getFullName(), a.getAppointmentDate(), a.getAppointmentTime(), a.getStatus(), a.getQueueNumber(),
            a.getQrPayload(), a.getReason(), record);
    }

    private String normalizePhone(String phone) { return phone.startsWith("+84") ? "0" + phone.substring(3) : phone; }
    private record OtpSession(String phone, String otp, Instant expiresAt) {}
    private record LookupSession(String phone, Instant expiresAt) {}
}
