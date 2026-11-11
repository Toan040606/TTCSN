package vn.edu.haui.bachmai.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.haui.bachmai.model.Patient;
import java.util.*;
public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByPhoneOrderByIdDesc(String phone);
}
