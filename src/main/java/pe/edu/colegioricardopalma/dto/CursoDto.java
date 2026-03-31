package pe.edu.colegioricardopalma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.Curso;
import pe.edu.colegioricardopalma.entity.Estado;
import pe.edu.colegioricardopalma.entity.Nivel;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursoDto {

    private UUID id;
    
    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
    
    @NotNull(message = "El nivel es requerido")
    private Nivel nivel;
    
    private Estado estado;

    public static CursoDto fromEntity(Curso entity) {
        return CursoDto.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .nivel(entity.getNivel())
                .estado(entity.getEstado())
                .build();
    }

    public Curso toEntity() {
        return Curso.builder()
                .id(this.id)
                .nombre(this.nombre)
                .nivel(this.nivel)
                .estado(this.estado != null ? this.estado : Estado.ACTIVO)
                .build();
    }
}
