package pe.edu.colegioricardopalma.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotaCreateRequest {

    @NotNull(message = "El alumno es requerido")
    private UUID alumnoId;

    @NotNull(message = "El curso es requerido")
    private UUID cursoId;

    @NotNull(message = "El bimestre es requerido")
    private UUID bimestreId;

    @DecimalMin(value = "0", message = "La nota n1 debe ser mayor o igual a 0")
    @DecimalMax(value = "20", message = "La nota n1 debe ser menor o igual a 20")
    private BigDecimal n1;

    @DecimalMin(value = "0", message = "La nota n2 debe ser mayor o igual a 0")
    @DecimalMax(value = "20", message = "La nota n2 debe ser menor o igual a 20")
    private BigDecimal n2;

    @DecimalMin(value = "0", message = "La nota n3 debe ser mayor o igual a 0")
    @DecimalMax(value = "20", message = "La nota n3 debe ser menor o igual a 20")
    private BigDecimal n3;

    @DecimalMin(value = "0", message = "La nota n4 debe ser mayor o igual a 0")
    @DecimalMax(value = "20", message = "La nota n4 debe ser menor o igual a 20")
    private BigDecimal n4;
}
