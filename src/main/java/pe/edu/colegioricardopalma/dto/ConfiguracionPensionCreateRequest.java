package pe.edu.colegioricardopalma.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionPensionCreateRequest {

    @NotNull(message = "El año escolar es requerido")
    private UUID anioEscolarId;

    @NotNull(message = "El monto base es requerido")
    @DecimalMin(value = "0.01", message = "El monto base debe ser mayor a 0")
    private BigDecimal montoBase;

    @Min(value = 1, message = "El día de vencimiento debe estar entre 1 y 28")
    @Max(value = 28, message = "El día de vencimiento debe estar entre 1 y 28")
    private Integer fechaVencimientoDia = 15;

    @DecimalMin(value = "0", message = "El porcentaje de mora no puede ser negativo")
    @DecimalMax(value = "100", message = "El porcentaje de mora no puede exceder 100%")
    private BigDecimal porcentajeMora = BigDecimal.ZERO;
}
