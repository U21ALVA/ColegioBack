package pe.edu.colegioricardopalma.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.DocenteCurso;
import pe.edu.colegioricardopalma.entity.Estado;
import pe.edu.colegioricardopalma.entity.Nivel;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocenteCursoDto {

    private UUID id;
    
    @NotNull(message = "El docente es requerido")
    private UUID docenteId;
    private String docenteNombres;
    private String docenteApellidos;
    private String docenteDni;
    
    @NotNull(message = "El curso es requerido")
    private UUID cursoId;
    private String cursoNombre;
    private Nivel cursoNivel;
    
    private UUID gradoId;
    private String gradoNombre;
    
    @NotNull(message = "La sección es requerida")
    private UUID seccionId;
    private String seccionNombre;
    
    @NotNull(message = "El año escolar es requerido")
    private UUID anioEscolarId;
    private Integer anio;
    
    private Estado estado;

    public static DocenteCursoDto fromEntity(DocenteCurso entity) {
        DocenteCursoDto dto = DocenteCursoDto.builder()
                .id(entity.getId())
                .estado(entity.getEstado())
                .build();
        
        if (entity.getDocente() != null) {
            dto.setDocenteId(entity.getDocente().getId());
            dto.setDocenteNombres(entity.getDocente().getNombres());
            dto.setDocenteApellidos(entity.getDocente().getApellidos());
            dto.setDocenteDni(entity.getDocente().getDni());
        }
        
        if (entity.getCurso() != null) {
            dto.setCursoId(entity.getCurso().getId());
            dto.setCursoNombre(entity.getCurso().getNombre());
            dto.setCursoNivel(entity.getCurso().getNivel());
        }
        
        if (entity.getGrado() != null) {
            dto.setGradoId(entity.getGrado().getId());
            dto.setGradoNombre(entity.getGrado().getNombre());
        }
        
        if (entity.getSeccion() != null) {
            dto.setSeccionId(entity.getSeccion().getId());
            dto.setSeccionNombre(entity.getSeccion().getNombre());
        }
        
        if (entity.getAnioEscolar() != null) {
            dto.setAnioEscolarId(entity.getAnioEscolar().getId());
            dto.setAnio(entity.getAnioEscolar().getAnio());
        }
        
        return dto;
    }

    public String getDocenteNombreCompleto() {
        if (docenteNombres == null || docenteApellidos == null) {
            return null;
        }
        return docenteNombres + " " + docenteApellidos;
    }

    public String getFullDescription() {
        return String.format("%s - %s %s", cursoNombre, gradoNombre, seccionNombre);
    }

    public String getCursoCodigo() {
        return cursoNombre;
    }

    public Nivel getGradoNivel() {
        return cursoNivel;
    }

    public String getAnioEscolarNombre() {
        return anio != null ? String.valueOf(anio) : null;
    }
}
