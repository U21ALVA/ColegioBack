package pe.edu.colegioricardopalma.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotaRecuperacionCreateRequest {

    @NotNull(message = "El alumno es requerido")
    private UUID alumnoId;

    @NotNull(message = "El curso es requerido")
    private UUID cursoId;

    @NotNull(message = "El año escolar es requerido")
    private UUID anioEscolarId;

    @DecimalMin(value = "0", message = "La nota original debe ser mayor o igual a 0")
    @DecimalMax(value = "20", message = "La nota original debe ser menor o igual a 20")
    private BigDecimal notaOriginal;

    @DecimalMin(value = "0", message = "La nota de recuperación debe ser mayor o igual a 0")
    @DecimalMax(value = "20", message = "La nota de recuperación debe ser menor o igual a 20")
    private BigDecimal notaRecuperacion;

    private LocalDate fechaExamen;

    private String observaciones;
}
