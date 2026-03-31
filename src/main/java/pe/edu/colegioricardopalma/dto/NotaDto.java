package pe.edu.colegioricardopalma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.LiteralNota;
import pe.edu.colegioricardopalma.entity.Nota;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotaDto {

    private UUID id;
    
    private UUID alumnoId;
    private String alumnoNombres;
    private String alumnoApellidos;
    private String alumnoCodigo;
    
    private UUID cursoId;
    private String cursoNombre;
    
    private UUID bimestreId;
    private Integer bimestreNumero;
    private Boolean bimestreCerrado;
    
    private BigDecimal n1;
    private BigDecimal n2;
    private BigDecimal n3;
    private BigDecimal n4;
    private BigDecimal notaFinal;
    
    private LiteralNota literal;
    private String literalDescripcion;
    
    private UUID docenteId;
    private String docenteNombre;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NotaDto fromEntity(Nota entity) {
        NotaDtoBuilder builder = NotaDto.builder()
                .id(entity.getId())
                .n1(entity.getN1())
                .n2(entity.getN2())
                .n3(entity.getN3())
                .n4(entity.getN4())
                .notaFinal(entity.getNotaFinal())
                .literal(entity.getLiteral())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        if (entity.getLiteral() != null) {
            builder.literalDescripcion(entity.getLiteral().getDescripcion());
        }

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

        if (entity.getBimestre() != null) {
            builder.bimestreId(entity.getBimestre().getId())
                   .bimestreNumero(entity.getBimestre().getNumero())
                   .bimestreCerrado(entity.getBimestre().getCerrado());
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
