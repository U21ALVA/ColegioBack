package pe.edu.colegioricardopalma.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nota_recuperacion", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"alumno_id", "curso_id", "anio_escolar_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaRecuperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anio_escolar_id", nullable = false)
    private AnioEscolar anioEscolar;

    @Column(name = "nota_original", precision = 4, scale = 2)
    private BigDecimal notaOriginal;

    @Column(name = "nota_recuperacion", precision = 4, scale = 2)
    private BigDecimal notaRecuperacion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean aprobado = false;

    @Column(name = "fecha_examen")
    private LocalDate fechaExamen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id")
    private Docente docente;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Update the aprobado status based on the recovery grade.
     * A grade >= 11 is considered passing.
     */
    public void calcularAprobado() {
        this.aprobado = notaRecuperacion != null && 
                        notaRecuperacion.compareTo(BigDecimal.valueOf(11)) >= 0;
    }
}
