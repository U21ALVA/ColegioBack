package pe.edu.colegioricardopalma.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotaBulkRequest {

    @NotNull(message = "El curso es requerido")
    private UUID cursoId;

    @NotNull(message = "El bimestre es requerido")
    private UUID bimestreId;

    @NotEmpty(message = "Debe incluir al menos una nota")
    @Valid
    private List<NotaAlumnoRequest> notas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotaAlumnoRequest {
        @NotNull(message = "El alumno es requerido")
        private UUID alumnoId;

        private java.math.BigDecimal n1;
        private java.math.BigDecimal n2;
        private java.math.BigDecimal n3;
        private java.math.BigDecimal n4;
    }
}
