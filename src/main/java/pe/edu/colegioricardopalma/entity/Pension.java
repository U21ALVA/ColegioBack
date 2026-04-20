package pe.edu.colegioricardopalma.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Pensión mensual de un alumno.
 * Representa la cuota mensual a pagar por un estudiante en un año escolar.
 */
@Entity
@Table(name = "pension", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"alumno_id", "anio_escolar_id", "mes"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pension {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anio_escolar_id", nullable = false)
    private AnioEscolar anioEscolar;

    @Column(nullable = false)
    private Integer mes;

    @Column(name = "monto_original", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "monto_final", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoFinal;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false, columnDefinition = "pension_estado")
    @Builder.Default
    private PensionEstado estado = PensionEstado.PENDIENTE;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Calcula y establece el monto final considerando el descuento.
     */
    public void calcularMontoFinal() {
        if (this.monto != null) {
            BigDecimal desc = this.descuento != null ? this.descuento : BigDecimal.ZERO;
            this.montoFinal = this.monto.subtract(desc);
            if (this.montoFinal.compareTo(BigDecimal.ZERO) < 0) {
                this.montoFinal = BigDecimal.ZERO;
            }
        }
    }

    /**
     * Obtiene el nombre del mes en español.
     */
    public String getNombreMes() {
        String[] meses = {"", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                         "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return (mes != null && mes >= 1 && mes <= 12) ? meses[mes] : "";
    }
}
