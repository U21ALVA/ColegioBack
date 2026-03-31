package pe.edu.colegioricardopalma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.Estado;
import pe.edu.colegioricardopalma.entity.Seccion;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeccionDto {

    private UUID id;
    
    @NotBlank(message = "El nombre es requerido")
    @Size(max = 10, message = "El nombre no puede exceder 10 caracteres")
    private String nombre;
    
    @NotNull(message = "El grado es requerido")
    private UUID gradoId;
    
    private String gradoNombre;
    private Integer capacidad;
    private Estado estado;
    private Long alumnosActivos;

    public static SeccionDto fromEntity(Seccion entity) {
        SeccionDto dto = SeccionDto.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .capacidad(entity.getCapacidad())
                .estado(entity.getEstado())
                .build();
        
        if (entity.getGrado() != null) {
            dto.setGradoId(entity.getGrado().getId());
            dto.setGradoNombre(entity.getGrado().getNombre());
        }
        
        return dto;
    }
}
