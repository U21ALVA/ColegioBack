package pe.edu.colegioricardopalma.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "alumno_apoderado", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"alumno_id", "apoderado_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlumnoApoderado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apoderado_id", nullable = false)
    private Apoderado apoderado;

    @Column(length = 50)
    private String parentesco;

    @Column(name = "es_principal")
    @Builder.Default
    private Boolean esPrincipal = false;
}
