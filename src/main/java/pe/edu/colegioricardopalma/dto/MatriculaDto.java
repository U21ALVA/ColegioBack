package pe.edu.colegioricardopalma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.AlumnoCurso;
import pe.edu.colegioricardopalma.entity.Estado;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatriculaDto {
    private UUID id;
    private UUID alumnoId;
    private String alumnoNombres;
    private String alumnoApellidos;
    private String alumnoCodigo;
    private UUID cursoId;
    private String cursoNombre;
    private UUID seccionId;
    private String seccionNombre;
    private UUID anioEscolarId;
    private Integer anioEscolar;
    private Estado estado;
    private String origen;
    private LocalDateTime createdAt;

    public static MatriculaDto fromEntity(AlumnoCurso ac) {
        return MatriculaDto.builder()
                .id(ac.getId())
                .alumnoId(ac.getAlumno() != null ? ac.getAlumno().getId() : null)
                .alumnoNombres(ac.getAlumno() != null ? ac.getAlumno().getNombres() : null)
                .alumnoApellidos(ac.getAlumno() != null ? ac.getAlumno().getApellidos() : null)
                .alumnoCodigo(ac.getAlumno() != null ? ac.getAlumno().getCodigoEstudiante() : null)
                .cursoId(ac.getCurso() != null ? ac.getCurso().getId() : null)
                .cursoNombre(ac.getCurso() != null ? ac.getCurso().getNombre() : null)
                .seccionId(ac.getSeccion() != null ? ac.getSeccion().getId() : null)
                .seccionNombre(ac.getSeccion() != null ? ac.getSeccion().getNombre() : null)
                .anioEscolarId(ac.getAnioEscolar() != null ? ac.getAnioEscolar().getId() : null)
                .anioEscolar(ac.getAnioEscolar() != null ? ac.getAnioEscolar().getAnio() : null)
                .estado(ac.getEstado())
                .origen(ac.getOrigen())
                .createdAt(ac.getCreatedAt())
                .build();
    }
}
