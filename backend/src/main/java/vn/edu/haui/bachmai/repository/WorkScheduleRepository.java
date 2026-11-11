package vn.edu.haui.bachmai.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.haui.bachmai.model.*;
import java.time.LocalDate;
import java.util.*;
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {
    List<WorkSchedule> findByDoctorIdAndWorkDateAndOpenTrue(Long doctorId, LocalDate date);
    List<WorkSchedule> findByWorkDateBetweenOrderByWorkDateAsc(LocalDate from, LocalDate to);
}
