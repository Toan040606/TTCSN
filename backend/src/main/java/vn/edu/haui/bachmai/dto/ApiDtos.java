package vn.edu.haui.bachmai.dto;

import jakarta.validation.constraints.*;
import vn.edu.haui.bachmai.model.*;
import java.time.*;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {}

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record LoginResponse(String token, Long userId, String username, String fullName, Role role) {}

    public record AppointmentRequest(
        @NotBlank String fullName,
        @NotBlank @Pattern(regexp = "^(0|\\+84)[0-9]{9}$") String phone,
        @Email String email,
        LocalDate dateOfBirth,
        String gender,
        String address,
        @NotNull Long facilityId,
        @NotNull Long specialtyId,
        @NotNull Long doctorId,
        @NotNull @FutureOrPresent LocalDate appointmentDate,
        @NotNull LocalTime appointmentTime,
        @NotBlank @Size(max = 1000) String reason
    ) {}

    public record AppointmentView(
        Long id, String code, String patientName, String phone, String email,
        String facilityName, String specialtyName, Long doctorId, String doctorName,
        LocalDate appointmentDate, LocalTime appointmentTime, AppointmentStatus status,
        Integer queueNumber, String qrPayload, String reason, MedicalRecordView medicalRecord
    ) {}

    public record OtpRequest(@NotBlank @Pattern(regexp = "^(0|\\+84)[0-9]{9}$") String phone) {}
    public record OtpResponse(String requestId, String message, String devOtp) {}
    public record OtpVerifyRequest(@NotBlank String requestId, @NotBlank String otp) {}
    public record OtpVerifyResponse(String lookupToken, List<AppointmentView> appointments) {}
    public record CancelRequest(@NotBlank String lookupToken) {}

    public record MedicalRecordRequest(@NotBlank String diagnosis, String symptoms, String prescription, String notes) {}
    public record MedicalRecordView(String symptoms, String diagnosis, String prescription, String notes, LocalDateTime updatedAt) {}

    public record UserRequest(@NotBlank String username, @NotBlank String fullName, @NotNull Role role, String phone, String password) {}
    public record UserView(Long id, String username, String fullName, String phone, Role role, boolean enabled) {}

    public record ScheduleRequest(@NotNull Long doctorId, @NotNull Long facilityId, @NotNull LocalDate workDate,
                                  @NotNull Shift shift, @Min(1) @Max(100) int capacity, @NotBlank String room) {}
    public record ScheduleView(Long id, Long doctorId, String doctorName, String specialtyName, String facilityName,
                               LocalDate workDate, Shift shift, int capacity, boolean open, String room) {}

    public record DashboardStats(long totalAppointments, long pending, long checkedIn, long completed, long cancelled,
                                 long totalDoctors, long totalPatients) {}
}
