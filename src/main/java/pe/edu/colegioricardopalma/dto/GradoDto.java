package pe.edu.colegioricardopalma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.Estado;
import pe.edu.colegioricardopalma.entity.Grado;
import pe.edu.colegioricardopalma.entity.Nivel;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradoDto {

    private UUID id;
    
    @NotBlank(message = "El nombre es requerido")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String nombre;
    
    @NotNull(message = "El nivel es requerido")
    private Nivel nivel;
    
    @NotNull(message = "El orden es requerido")
    private Integer orden;
    
    private Estado estado;
    private List<SeccionDto> secciones;

    public static GradoDto fromEntity(Grado entity) {
        return GradoDto.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .nivel(entity.getNivel())
                .orden(entity.getOrden())
                .estado(entity.getEstado())
                .build();
    }

    public static GradoDto fromEntityWithSecciones(Grado entity) {
        GradoDto dto = fromEntity(entity);
        if (entity.getSecciones() != null) {
            dto.setSecciones(entity.getSecciones().stream()
                    .map(SeccionDto::fromEntity)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public Grado toEntity() {
        return Grado.builder()
                .id(this.id)
                .nombre(this.nombre)
                .nivel(this.nivel)
                .orden(this.orden)
                .estado(this.estado != null ? this.estado : Estado.ACTIVO)
                .build();
    }
}
