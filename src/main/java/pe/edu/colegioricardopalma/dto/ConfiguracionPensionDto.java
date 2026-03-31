package pe.edu.colegioricardopalma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.ConfiguracionPension;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionPensionDto {

    private UUID id;
    private UUID anioEscolarId;
    private Integer anioEscolar;
    private BigDecimal montoBase;
    private Integer fechaVencimientoDia;
    private BigDecimal porcentajeMora;

    public static ConfiguracionPensionDto fromEntity(ConfiguracionPension entity) {
        if (entity == null) return null;
        
        return ConfiguracionPensionDto.builder()
                .id(entity.getId())
                .anioEscolarId(entity.getAnioEscolar() != null ? entity.getAnioEscolar().getId() : null)
                .anioEscolar(entity.getAnioEscolar() != null ? entity.getAnioEscolar().getAnio() : null)
                .montoBase(entity.getMontoBase())
                .fechaVencimientoDia(entity.getFechaVencimientoDia())
                .porcentajeMora(entity.getPorcentajeMora())
                .build();
    }
}
