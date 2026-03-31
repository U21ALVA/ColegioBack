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
public class PensionCreateRequest {

    @NotNull(message = "El alumno es requerido")
    private UUID alumnoId;

    @NotNull(message = "El año escolar es requerido")
    private UUID anioEscolarId;

    @NotNull(message = "El mes es requerido")
    @Min(value = 1, message = "El mes debe estar entre 1 y 12")
    @Max(value = 12, message = "El mes debe estar entre 1 y 12")
    private Integer mes;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @DecimalMin(value = "0", message = "El descuento no puede ser negativo")
    private BigDecimal descuento;
}
