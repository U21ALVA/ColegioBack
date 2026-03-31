package pe.edu.colegioricardopalma.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.Bimestre;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BimestreDto {

    private UUID id;
    
    @NotNull(message = "El número de bimestre es requerido")
    @Min(value = 1, message = "El número de bimestre debe ser mínimo 1")
    @Max(value = 4, message = "El número de bimestre debe ser máximo 4")
    private Integer numero;
    
    @NotNull(message = "El año escolar es requerido")
    private UUID anioEscolarId;
    
    private Integer anio;
    
    @NotNull(message = "La fecha de inicio es requerida")
    private LocalDate fechaInicio;
    
    @NotNull(message = "La fecha de fin es requerida")
    private LocalDate fechaFin;
    
    private Boolean cerrado;

    public static BimestreDto fromEntity(Bimestre entity) {
        BimestreDto dto = BimestreDto.builder()
                .id(entity.getId())
                .numero(entity.getNumero())
                .fechaInicio(entity.getFechaInicio())
                .fechaFin(entity.getFechaFin())
                .cerrado(entity.getCerrado())
                .build();
        
        if (entity.getAnioEscolar() != null) {
            dto.setAnioEscolarId(entity.getAnioEscolar().getId());
            dto.setAnio(entity.getAnioEscolar().getAnio());
        }
        
        return dto;
    }
}
