package vn.edu.haui.bachmai.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.haui.bachmai.model.MedicalRecord;
import java.util.Optional;
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);
}
