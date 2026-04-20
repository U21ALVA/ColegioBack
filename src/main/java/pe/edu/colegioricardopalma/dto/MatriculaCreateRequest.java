package pe.edu.colegioricardopalma.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatriculaCreateRequest {
    @NotNull
    private UUID alumnoId;
    @NotNull
    private UUID cursoId;
    @NotNull
    private UUID seccionId;
    @NotNull
    private UUID anioEscolarId;
}
