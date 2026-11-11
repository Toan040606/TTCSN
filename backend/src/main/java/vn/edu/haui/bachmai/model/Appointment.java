package vn.edu.haui.bachmai.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Table(indexes = {
    @Index(name = "idx_appointment_code", columnList = "code", unique = true),
    @Index(name = "idx_appointment_date", columnList = "appointmentDate")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Appointment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 24)
    private String code;
    @ManyToOne(optional = false)
    private Patient patient;
    @ManyToOne(optional = false)
    private Facility facility;
    @ManyToOne(optional = false)
    private Specialty specialty;
    @ManyToOne(optional = false)
    private Doctor doctor;
    @Column(nullable = false)
    private LocalDate appointmentDate;
    @Column(nullable = false)
    private LocalTime appointmentTime;
    @Column(nullable = false)
    private String reason;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private AppointmentStatus status;
    private Integer queueNumber;
    private String qrPayload;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime checkedInAt;

    @PrePersist
    void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}
