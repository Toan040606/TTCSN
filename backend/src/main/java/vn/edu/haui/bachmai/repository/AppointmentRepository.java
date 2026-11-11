package vn.edu.haui.bachmai.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.haui.bachmai.model.*;
import java.time.*;
import java.util.*;
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Optional<Appointment> findByCode(String code);
    List<Appointment> findByPatientPhoneOrderByCreatedAtDesc(String phone);
    List<Appointment> findByDoctorIdAndAppointmentDateOrderByAppointmentTime(Long doctorId, LocalDate date);
    List<Appointment> findByAppointmentDateOrderByAppointmentTime(LocalDate date);
    long countByDoctorIdAndAppointmentDateAndAppointmentTimeBetweenAndStatusNot(Long doctorId, LocalDate date, LocalTime from, LocalTime to, AppointmentStatus status);
    long countByStatus(AppointmentStatus status);
}
