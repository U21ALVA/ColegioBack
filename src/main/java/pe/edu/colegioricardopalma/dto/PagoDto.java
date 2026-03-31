package pe.edu.colegioricardopalma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.Pago;
import pe.edu.colegioricardopalma.entity.PagoEstado;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoDto {

    private UUID id;
    private UUID pensionId;
    private String alumnoNombres;
    private String alumnoApellidos;
    private Integer mes;
    private String stripePaymentIntentId;
    private String stripeCheckoutSessionId;
    private BigDecimal monto;
    private PagoEstado estado;
    private String metodoPago;
    private LocalDateTime fechaPago;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;

    public static PagoDto fromEntity(Pago entity) {
        if (entity == null) return null;

        var pension = entity.getPension();
        var alumno = pension != null ? pension.getAlumno() : null;

        return PagoDto.builder()
                .id(entity.getId())
                .pensionId(pension != null ? pension.getId() : null)
                .alumnoNombres(alumno != null ? alumno.getNombres() : null)
                .alumnoApellidos(alumno != null ? alumno.getApellidos() : null)
                .mes(pension != null ? pension.getMes() : null)
                .stripePaymentIntentId(entity.getStripePaymentIntentId())
                .stripeCheckoutSessionId(entity.getStripeCheckoutSessionId())
                .monto(entity.getMonto())
                .estado(entity.getEstado())
                .metodoPago(entity.getMetodoPago())
                .fechaPago(entity.getFechaPago())
                .metadata(entity.getMetadata())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
