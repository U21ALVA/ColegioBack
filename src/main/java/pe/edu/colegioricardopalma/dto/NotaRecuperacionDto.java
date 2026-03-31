package pe.edu.colegioricardopalma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.NotaRecuperacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotaRecuperacionDto {

    private UUID id;
    
    private UUID alumnoId;
    private String alumnoNombres;
    private String alumnoApellidos;
    private String alumnoCodigo;
    
    private UUID cursoId;
    private String cursoNombre;
    
    private UUID anioEscolarId;
    private Integer anioEscolar;
    
    private BigDecimal notaOriginal;
    private BigDecimal notaRecuperacion;
    private Boolean aprobado;
    private LocalDate fechaExamen;
    
    private UUID docenteId;
    private String docenteNombre;
    
    private String observaciones;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NotaRecuperacionDto fromEntity(NotaRecuperacion entity) {
        NotaRecuperacionDtoBuilder builder = NotaRecuperacionDto.builder()
                .id(entity.getId())
                .notaOriginal(entity.getNotaOriginal())
                .notaRecuperacion(entity.getNotaRecuperacion())
                .aprobado(entity.getAprobado())
                .fechaExamen(entity.getFechaExamen())
                .observaciones(entity.getObservaciones())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        if (entity.getAlumno() != null) {
            builder.alumnoId(entity.getAlumno().getId())
                   .alumnoNombres(entity.getAlumno().getNombres())
                   .alumnoApellidos(entity.getAlumno().getApellidos())
                   .alumnoCodigo(entity.getAlumno().getCodigoEstudiante());
        }

        if (entity.getCurso() != null) {
            builder.cursoId(entity.getCurso().getId())
                   .cursoNombre(entity.getCurso().getNombre());
        }

        if (entity.getAnioEscolar() != null) {
            builder.anioEscolarId(entity.getAnioEscolar().getId())
                   .anioEscolar(entity.getAnioEscolar().getAnio());
        }

        if (entity.getDocente() != null) {
            builder.docenteId(entity.getDocente().getId())
                   .docenteNombre(entity.getDocente().getNombreCompleto());
        }

        return builder.build();
    }

    public String getAlumnoNombreCompleto() {
        if (alumnoNombres == null || alumnoApellidos == null) {
            return null;
        }
        return alumnoNombres + " " + alumnoApellidos;
    }
}
