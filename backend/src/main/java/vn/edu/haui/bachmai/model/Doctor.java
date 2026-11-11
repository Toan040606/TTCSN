package vn.edu.haui.bachmai.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Doctor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 30)
    private String code;
    @Column(nullable = false)
    private String fullName;
    private String title;
    @Column(length = 1200)
    private String bio;
    private String avatarUrl;
    @ManyToOne(optional = false)
    private Specialty specialty;
    @ManyToOne(optional = false)
    private Facility facility;
    @OneToOne
    private UserAccount account;
    private boolean active = true;
}
