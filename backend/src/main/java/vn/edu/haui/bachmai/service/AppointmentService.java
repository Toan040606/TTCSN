package vn.edu.haui.bachmai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
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
    private final PrescriptionItemRepository prescriptionItems;
    private final NotificationService notificationService;
    @Value("${app.expose-dev-otp:false}")
    private boolean exposeDevOtp;
    private final Map<String, OtpSession> otpSessions = new ConcurrentHashMap<>();
    private final Map<String, LookupSession> lookupSessions = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastOtpRequests = new ConcurrentHashMap<>();

    @Transactional
    public AppointmentView create(AppointmentRequest r) {
        var facility = facilities.findById(r.facilityId()).orElseThrow(() -> new IllegalArgumentException("Cơ sở khám không tồn tại"));
        var specialty = specialties.findById(r.specialtyId()).orElseThrow(() -> new IllegalArgumentException("Chuyên khoa không tồn tại"));
        var doctor = doctors.findById(r.doctorId()).orElseThrow(() -> new IllegalArgumentException("Bác sĩ không tồn tại"));
        if (!doctor.isActive() || !doctor.getSpecialty().getId().equals(specialty.getId()))
            throw new IllegalArgumentException("Bác sĩ không thuộc chuyên khoa đã chọn");
        if (!doctor.getFacility().getId().equals(facility.getId()))
            throw new IllegalArgumentException("Bác sĩ không làm việc tại cơ sở đã chọn");
        Shift shift = shiftOf(r.appointmentTime());
        WorkSchedule schedule = schedules.lockOpenSchedule(doctor.getId(), r.appointmentDate(), shift)
            .orElseThrow(() -> new IllegalArgumentException("Khung giờ không thuộc lịch làm việc đang mở của bác sĩ"));
        if (!schedule.getFacility().getId().equals(facility.getId()))
            throw new IllegalArgumentException("Ca trực của bác sĩ không thuộc cơ sở đã chọn");
        int slotCapacity = slotCapacity(schedule);
        long used = appointments.countByDoctorIdAndAppointmentDateAndAppointmentTimeBetweenAndStatusNot(
            doctor.getId(), r.appointmentDate(), r.appointmentTime(), r.appointmentTime(), AppointmentStatus.CANCELLED);
        if (!isValidSlot(r.appointmentTime(), shift) || used >= slotCapacity ||
            (r.appointmentDate().equals(LocalDate.now()) && r.appointmentTime().isBefore(LocalTime.now())))
            throw new IllegalArgumentException("Khung giờ đã hết chỗ hoặc không hợp lệ");

        Patient patient = Patient.builder().fullName(r.fullName()).phone(normalizePhone(r.phone())).email(r.email())
            .dateOfBirth(r.dateOfBirth()).gender(r.gender()).address(r.address()).build();
        patients.save(patient);
        String code = "BM" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + String.format("%04d", new Random().nextInt(10000));
        while (appointments.findByCode(code).isPresent()) code = code.substring(0, code.length() - 4) + String.format("%04d", new Random().nextInt(10000));
        Appointment appointment = Appointment.builder().code(code).patient(patient).facility(facility).specialty(specialty)
            .doctor(doctor).appointmentDate(r.appointmentDate()).appointmentTime(r.appointmentTime()).reason(r.reason())
            .status(AppointmentStatus.CONFIRMED).qrPayload("BACHMAI:" + code).build();
        appointment = appointments.save(appointment);
        notificationService.bookingConfirmed(appointment);
        return view(appointment);
    }

    public List<LocalTime> availableTimes(Long doctorId, LocalDate date) {
        if (date.isBefore(LocalDate.now())) return List.of();
        List<LocalTime> result = new ArrayList<>();
        for (WorkSchedule schedule : schedules.findByDoctorIdAndWorkDateAndOpenTrue(doctorId, date)) {
            LocalTime start = schedule.getShift() == Shift.MORNING ? LocalTime.of(7, 0) : LocalTime.of(13, 30);
            LocalTime end = schedule.getShift() == Shift.MORNING ? LocalTime.of(11, 30) : LocalTime.of(17, 0);
            int slotCapacity = slotCapacity(schedule);
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
        Instant lastRequest = lastOtpRequests.get(phone);
        if (lastRequest != null && lastRequest.plusSeconds(60).isAfter(Instant.now()))
            throw new IllegalArgumentException("Vui lòng chờ 60 giây trước khi yêu cầu mã OTP mới");
        String requestId = UUID.randomUUID().toString();
        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        otpSessions.put(requestId, new OtpSession(phone, otp, Instant.now().plusSeconds(300), 0));
        lastOtpRequests.put(phone, Instant.now());
        return new OtpResponse(requestId, "Mã OTP có hiệu lực trong 5 phút", exposeDevOtp ? otp : null);
    }

    public OtpVerifyResponse verifyOtp(OtpVerifyRequest request) {
        OtpSession session = otpSessions.get(request.requestId());
        if (session == null || session.expiresAt().isBefore(Instant.now()))
            throw new IllegalArgumentException("Mã OTP không chính xác hoặc đã hết hiệu lực");
        if (!session.otp().equals(request.otp())) {
            int attempts = session.attempts() + 1;
            if (attempts >= 5) otpSessions.remove(request.requestId());
            else otpSessions.put(request.requestId(), new OtpSession(session.phone(), session.otp(), session.expiresAt(), attempts));
            throw new IllegalArgumentException(attempts >= 5 ? "Mã OTP đã bị khóa sau 5 lần nhập sai" : "Mã OTP không chính xác");
        }
        otpSessions.remove(request.requestId());
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
        notificationService.bookingCancelled(appointment);
        return view(appointment);
    }

    @Transactional
    public AppointmentView reschedule(String code, RescheduleRequest request) {
        LookupSession session = lookupSessions.get(request.lookupToken());
        Appointment a = appointments.findByCode(code).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn"));
        if (session == null || session.expiresAt().isBefore(Instant.now()) || !session.phone().equals(a.getPatient().getPhone()))
            throw new IllegalArgumentException("Phiên tra cứu không hợp lệ hoặc đã hết hạn");
        if (!List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED).contains(a.getStatus()))
            throw new IllegalArgumentException("Lịch hẹn ở trạng thái hiện tại không thể thay đổi");
        Shift shift = shiftOf(request.appointmentTime());
        WorkSchedule schedule = schedules.lockOpenSchedule(a.getDoctor().getId(), request.appointmentDate(), shift)
            .orElseThrow(() -> new IllegalArgumentException("Khung giờ không thuộc lịch làm việc đang mở của bác sĩ"));
        if (!schedule.getFacility().getId().equals(a.getFacility().getId()))
            throw new IllegalArgumentException("Ca trực mới không thuộc cơ sở của lịch hẹn");
        long used = appointments.countByDoctorIdAndAppointmentDateAndAppointmentTimeBetweenAndStatusNot(
            a.getDoctor().getId(), request.appointmentDate(), request.appointmentTime(), request.appointmentTime(), AppointmentStatus.CANCELLED);
        if (a.getAppointmentDate().equals(request.appointmentDate()) && a.getAppointmentTime().equals(request.appointmentTime())) used--;
        if (!isValidSlot(request.appointmentTime(), shift) || used >= slotCapacity(schedule))
            throw new IllegalArgumentException("Khung giờ mới đã hết chỗ hoặc không hợp lệ");
        a.setAppointmentDate(request.appointmentDate());
        a.setAppointmentTime(request.appointmentTime());
        a.setReminderSentAt(null);
        notificationService.bookingChanged(a);
        return view(a);
    }

    public AppointmentView view(Appointment a) {
        MedicalRecordView record = records.findByAppointmentId(a.getId())
            .map(r -> new MedicalRecordView(r.getSymptoms(), r.getDiagnosis(), r.getPrescription(), r.getNotes(), r.getUpdatedAt(),
                prescriptionItems.findByMedicalRecordIdOrderById(r.getId()).stream().map(i ->
                    new MedicationItem(i.getName(), i.getDosage(), i.getFrequency(), i.getDays(), i.getInstruction())).toList())).orElse(null);
        return new AppointmentView(a.getId(), a.getCode(), a.getPatient().getFullName(), a.getPatient().getPhone(),
            a.getPatient().getEmail(), a.getFacility().getName(), a.getSpecialty().getName(), a.getDoctor().getId(),
            a.getDoctor().getFullName(), a.getAppointmentDate(), a.getAppointmentTime(), a.getStatus(), a.getQueueNumber(),
            a.getQrPayload(), a.getReason(), record);
    }

    private String normalizePhone(String phone) { return phone.startsWith("+84") ? "0" + phone.substring(3) : phone; }
    private Shift shiftOf(LocalTime time) {
        if (!time.isBefore(LocalTime.of(7, 0)) && time.isBefore(LocalTime.of(11, 30))) return Shift.MORNING;
        if (!time.isBefore(LocalTime.of(13, 30)) && time.isBefore(LocalTime.of(17, 0))) return Shift.AFTERNOON;
        throw new IllegalArgumentException("Giờ khám không thuộc ca sáng hoặc ca chiều");
    }
    private boolean isValidSlot(LocalTime time, Shift shift) {
        LocalTime start = shift == Shift.MORNING ? LocalTime.of(7, 0) : LocalTime.of(13, 30);
        return !time.isBefore(start) && Duration.between(start, time).toMinutes() % 30 == 0;
    }
    private int slotCapacity(WorkSchedule schedule) {
        LocalTime start = schedule.getShift() == Shift.MORNING ? LocalTime.of(7, 0) : LocalTime.of(13, 30);
        LocalTime end = schedule.getShift() == Shift.MORNING ? LocalTime.of(11, 30) : LocalTime.of(17, 0);
        return Math.max(1, (int) Math.ceil(schedule.getCapacity() / (double) (Duration.between(start, end).toMinutes() / 30)));
    }
    private record OtpSession(String phone, String otp, Instant expiresAt, int attempts) {}
    private record LookupSession(String phone, Instant expiresAt) {}
}
