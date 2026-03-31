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
public class BecaCreateRequest {

    @NotNull(message = "El alumno es requerido")
    private UUID alumnoId;

    @NotNull(message = "El año escolar es requerido")
    private UUID anioEscolarId;

    @NotBlank(message = "El tipo de beca es requerido")
    private String tipo;  // ACADEMICA, DEPORTIVA, SOCIAL

    @NotNull(message = "El porcentaje es requerido")
    @DecimalMin(value = "0.01", message = "El porcentaje debe ser mayor a 0")
    @DecimalMax(value = "100", message = "El porcentaje no puede exceder 100%")
    private BigDecimal porcentaje;

    private String motivo;
}
