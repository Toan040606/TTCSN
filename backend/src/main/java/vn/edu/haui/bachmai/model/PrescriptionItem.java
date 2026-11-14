package vn.edu.haui.bachmai.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PrescriptionItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private MedicalRecord medicalRecord;
    @Column(nullable = false)
    private String name;
    private String dosage;
    private String frequency;
    private Integer days;
    private String instruction;
}
