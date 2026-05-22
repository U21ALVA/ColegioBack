package pe.edu.colegioricardopalma.dto;

import jakarta.validation.constraints.NotBlank;
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
public class SiagieExportRequest {

    @NotNull(message = "El año escolar es requerido")
    private UUID anioEscolarId;

    @NotNull(message = "El bimestre es requerido")
    private UUID bimestreId;

    private List<UUID> cursoIds;

    private UUID gradoId;

    private UUID seccionId;

    @NotBlank(message = "El tipo es requerido")
    private String tipo;

    // XLSX (default), CSV, PDF
    private String formato;
}
