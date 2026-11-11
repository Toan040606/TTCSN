package vn.edu.haui.bachmai.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Specialty {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 30)
    private String code;
    @Column(nullable = false)
    private String name;
    @Column(length = 800)
    private String description;
    private String icon;
}
