package pe.edu.colegioricardopalma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.ConfiguracionSiagie;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionSiagieDto {

    private UUID id;

    @NotBlank(message = "La institución educativa es requerida")
    private String institucionEducativa;

    @NotBlank(message = "El código modular/anexo es requerido")
    private String codigoModularAnexo;

    @NotBlank(message = "El nivel es requerido")
    private String nivel;

    @NotBlank(message = "El nombre del reporte es requerido")
    private String nombreReporte;

    @NotNull(message = "El año académico es requerido")
    private Integer anioAcademico;

    private String disenoModular;
    private String periodo;
    private String grado;
    private String seccion;
    private String areasCursos;
    private UUID createdBy;

    public static ConfiguracionSiagieDto fromEntity(ConfiguracionSiagie entity) {
        if (entity == null) return null;

        return ConfiguracionSiagieDto.builder()
                .id(entity.getId())
                .institucionEducativa(entity.getInstitucionEducativa())
                .codigoModularAnexo(entity.getCodigoModularAnexo())
                .nivel(entity.getNivel())
                .nombreReporte(entity.getNombreReporte())
                .anioAcademico(entity.getAnioAcademico())
                .disenoModular(entity.getDisenoModular())
                .periodo(entity.getPeriodo())
                .grado(entity.getGrado())
                .seccion(entity.getSeccion())
                .areasCursos(entity.getAreasCursos())
                .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)
                .build();
    }
}
