package vn.edu.haui.bachmai.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(indexes = @Index(name = "idx_notification_created", columnList = "createdAt"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Appointment appointment;
    @Column(nullable = false, length = 20)
    private String channel;
    @Column(nullable = false, length = 30)
    private String type;
    @Column(nullable = false)
    private String recipient;
    @Column(nullable = false, length = 2000)
    private String content;
    @Column(nullable = false, length = 20)
    private String status;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    @PrePersist
    void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}
