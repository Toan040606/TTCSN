package vn.edu.haui.bachmai.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import vn.edu.haui.bachmai.model.*;
import java.time.LocalDate;
import java.util.*;
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {
    List<WorkSchedule> findByDoctorIdAndWorkDateAndOpenTrue(Long doctorId, LocalDate date);
    List<WorkSchedule> findByWorkDateBetweenOrderByWorkDateAsc(LocalDate from, LocalDate to);
    Optional<WorkSchedule> findByDoctorIdAndWorkDateAndShift(Long doctorId, LocalDate date, Shift shift);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from WorkSchedule s where s.doctor.id = :doctorId and s.workDate = :date and s.shift = :shift and s.open = true")
    Optional<WorkSchedule> lockOpenSchedule(@Param("doctorId") Long doctorId, @Param("date") LocalDate date, @Param("shift") Shift shift);
}
