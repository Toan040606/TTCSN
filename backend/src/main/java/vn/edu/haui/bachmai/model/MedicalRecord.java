package vn.edu.haui.bachmai.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicalRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false)
    private Appointment appointment;
    @Column(length = 1200)
    private String symptoms;
    @Column(length = 1200)
    private String diagnosis;
    @Column(length = 2000)
    private String prescription;
    @Column(length = 1200)
    private String notes;
    private LocalDateTime updatedAt;
}
