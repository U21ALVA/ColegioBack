package pe.edu.colegioricardopalma.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nota", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"alumno_id", "curso_id", "bimestre_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nota {

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
    @JoinColumn(name = "bimestre_id", nullable = false)
    private Bimestre bimestre;

    @Column(precision = 4, scale = 2)
    private BigDecimal n1;

    @Column(precision = 4, scale = 2)
    private BigDecimal n2;

    @Column(precision = 4, scale = 2)
    private BigDecimal n3;

    @Column(precision = 4, scale = 2)
    private BigDecimal n4;

    @Column(name = "nota_final", precision = 4, scale = 2)
    private BigDecimal notaFinal;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "literal", columnDefinition = "literal_nota")
    private LiteralNota literal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id")
    private Docente docente;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Calculate the final grade as the average of n1, n2, n3, n4.
     * Only non-null values are considered.
     */
    public void calcularNotaFinal() {
        int count = 0;
        BigDecimal sum = BigDecimal.ZERO;

        if (n1 != null) {
            sum = sum.add(n1);
            count++;
        }
        if (n2 != null) {
            sum = sum.add(n2);
            count++;
        }
        if (n3 != null) {
            sum = sum.add(n3);
            count++;
        }
        if (n4 != null) {
            sum = sum.add(n4);
            count++;
        }

        if (count > 0) {
            this.notaFinal = sum.divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP);
            this.literal = LiteralNota.fromNota(this.notaFinal.doubleValue());
        } else {
            this.notaFinal = null;
            this.literal = null;
        }
    }
}
