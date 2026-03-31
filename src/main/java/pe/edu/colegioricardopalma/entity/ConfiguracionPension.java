package pe.edu.colegioricardopalma.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Configuración de pensión por año escolar.
 * Define el monto base, día de vencimiento y porcentaje de mora.
 */
@Entity
@Table(name = "configuracion_pension")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionPension {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anio_escolar_id", nullable = false)
    private AnioEscolar anioEscolar;

    @Column(name = "monto_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoBase;

    @Column(name = "fecha_vencimiento_dia")
    @Builder.Default
    private Integer fechaVencimientoDia = 15;

    @Column(name = "porcentaje_mora", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeMora = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
