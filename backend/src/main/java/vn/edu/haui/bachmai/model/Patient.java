package vn.edu.haui.bachmai.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(indexes = @Index(name = "idx_patient_phone", columnList = "phone"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Patient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String fullName;
    @Column(nullable = false, length = 20)
    private String phone;
    private String email;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
}
