package vn.edu.haui.bachmai.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"doctor_id", "workDate", "shift"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkSchedule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Doctor doctor;
    @ManyToOne(optional = false)
    private Facility facility;
    @Column(nullable = false)
    private LocalDate workDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Shift shift;
    @Column(nullable = false)
    private int capacity;
    @Column(nullable = false)
    private boolean open = true;
    private String room;
}
