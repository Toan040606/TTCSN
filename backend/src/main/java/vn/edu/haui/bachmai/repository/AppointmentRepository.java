package vn.edu.haui.bachmai.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.haui.bachmai.model.*;
import java.time.*;
import java.util.*;
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Optional<Appointment> findByCode(String code);
    Optional<Appointment> findByQrPayload(String qrPayload);
    List<Appointment> findByPatientPhoneOrderByCreatedAtDesc(String phone);
    List<Appointment> findByDoctorIdAndAppointmentDateOrderByAppointmentTime(Long doctorId, LocalDate date);
    List<Appointment> findByAppointmentDateOrderByAppointmentTime(LocalDate date);
    List<Appointment> findByFacilityIdAndAppointmentDateOrderByAppointmentTime(Long facilityId, LocalDate date);
    List<Appointment> findByFacilityIdAndPatientPhoneOrderByCreatedAtDesc(Long facilityId, String phone);
    long countByDoctorIdAndAppointmentDateAndAppointmentTimeBetweenAndStatusNot(Long doctorId, LocalDate date, LocalTime from, LocalTime to, AppointmentStatus status);
    long countByStatus(AppointmentStatus status);
    List<Appointment> findByAppointmentDateAndStatusIn(LocalDate date, Collection<AppointmentStatus> statuses);
}
