package pe.edu.colegioricardopalma.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Beca o descuento aplicable a un alumno para un año escolar.
 */
@Entity
@Table(name = "beca", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"alumno_id", "anio_escolar_id", "tipo"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Beca {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anio_escolar_id", nullable = false)
    private AnioEscolar anioEscolar;

    @Column(nullable = false, length = 50)
    private String tipo;  // ACADEMICA, DEPORTIVA, SOCIAL, etc.

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "vigente")
    @Builder.Default
    private Boolean vigente = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobado_por")
    private Usuario aprobadoPor;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}
