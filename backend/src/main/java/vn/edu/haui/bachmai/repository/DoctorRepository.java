package vn.edu.haui.bachmai.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.haui.bachmai.model.Doctor;
import java.util.*;
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByActiveTrue();
    List<Doctor> findBySpecialtyIdAndFacilityIdAndActiveTrue(Long specialtyId, Long facilityId);
    Optional<Doctor> findByAccountUsername(String username);
}
