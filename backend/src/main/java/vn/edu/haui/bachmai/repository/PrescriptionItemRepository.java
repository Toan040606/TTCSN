package vn.edu.haui.bachmai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.haui.bachmai.model.PrescriptionItem;
import java.util.List;

public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Long> {
    List<PrescriptionItem> findByMedicalRecordIdOrderById(Long medicalRecordId);
    void deleteByMedicalRecordId(Long medicalRecordId);
}
