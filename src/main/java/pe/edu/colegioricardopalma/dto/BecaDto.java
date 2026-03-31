package pe.edu.colegioricardopalma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.Beca;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BecaDto {

    private UUID id;
    private UUID alumnoId;
    private String alumnoNombres;
    private String alumnoApellidos;
    private String alumnoCodigo;
    private UUID anioEscolarId;
    private Integer anioEscolar;
    private String tipo;
    private BigDecimal porcentaje;
    private String motivo;
    private UUID aprobadoPorId;
    private String aprobadoPorNombre;
    private Boolean vigente;

    public static BecaDto fromEntity(Beca entity) {
        if (entity == null) return null;

        var alumno = entity.getAlumno();
        var anioEscolar = entity.getAnioEscolar();
        var aprobadoPor = entity.getAprobadoPor();

        return BecaDto.builder()
                .id(entity.getId())
                .alumnoId(alumno != null ? alumno.getId() : null)
                .alumnoNombres(alumno != null ? alumno.getNombres() : null)
                .alumnoApellidos(alumno != null ? alumno.getApellidos() : null)
                .alumnoCodigo(alumno != null ? alumno.getCodigoEstudiante() : null)
                .anioEscolarId(anioEscolar != null ? anioEscolar.getId() : null)
                .anioEscolar(anioEscolar != null ? anioEscolar.getAnio() : null)
                .tipo(entity.getTipo())
                .porcentaje(entity.getPorcentaje())
                .motivo(entity.getMotivo())
                .aprobadoPorId(aprobadoPor != null ? aprobadoPor.getId() : null)
                .aprobadoPorNombre(aprobadoPor != null ? aprobadoPor.getUsername() : null)
                .vigente(entity.getVigente())
                .build();
    }
}
