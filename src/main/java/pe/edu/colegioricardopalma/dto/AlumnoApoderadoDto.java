package pe.edu.colegioricardopalma.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.AlumnoApoderado;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlumnoApoderadoDto {

    private UUID id;
    
    @NotNull(message = "El alumno es requerido")
    private UUID alumnoId;
    private String alumnoNombres;
    private String alumnoApellidos;
    private String alumnoDni;
    private String gradoNombre;
    
    @NotNull(message = "El apoderado es requerido")
    private UUID apoderadoId;
    private String apoderadoNombres;
    private String apoderadoApellidos;
    private String apoderadoDni;
    
    private String parentesco;
    private Boolean esPrincipal;

    public static AlumnoApoderadoDto fromEntity(AlumnoApoderado entity) {
        AlumnoApoderadoDto dto = AlumnoApoderadoDto.builder()
                .id(entity.getId())
                .parentesco(entity.getParentesco())
                .esPrincipal(entity.getEsPrincipal())
                .build();
        
        if (entity.getAlumno() != null) {
            dto.setAlumnoId(entity.getAlumno().getId());
            dto.setAlumnoNombres(entity.getAlumno().getNombres());
            dto.setAlumnoApellidos(entity.getAlumno().getApellidos());
            dto.setAlumnoDni(entity.getAlumno().getDni());
            if (entity.getAlumno().getGrado() != null) {
                dto.setGradoNombre(entity.getAlumno().getGrado().getNombre());
            }
        }
        
        if (entity.getApoderado() != null) {
            dto.setApoderadoId(entity.getApoderado().getId());
            dto.setApoderadoNombres(entity.getApoderado().getNombres());
            dto.setApoderadoApellidos(entity.getApoderado().getApellidos());
            dto.setApoderadoDni(entity.getApoderado().getDni());
        }
        
        return dto;
    }
}
