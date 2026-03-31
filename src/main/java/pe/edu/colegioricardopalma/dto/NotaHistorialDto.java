package pe.edu.colegioricardopalma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.NotaHistorial;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotaHistorialDto {

    private UUID id;
    
    private UUID notaId;
    private String alumnoNombre;
    private String cursoNombre;
    private Integer bimestreNumero;
    
    private String campoModificado;
    private String valorAnterior;
    private String valorNuevo;
    
    private UUID usuarioId;
    private String usuarioNombre;
    
    private LocalDateTime createdAt;

    public static NotaHistorialDto fromEntity(NotaHistorial entity) {
        NotaHistorialDtoBuilder builder = NotaHistorialDto.builder()
                .id(entity.getId())
                .campoModificado(entity.getCampoModificado())
                .valorAnterior(entity.getValorAnterior())
                .valorNuevo(entity.getValorNuevo())
                .createdAt(entity.getCreatedAt());

        if (entity.getNota() != null) {
            builder.notaId(entity.getNota().getId());
            if (entity.getNota().getAlumno() != null) {
                builder.alumnoNombre(entity.getNota().getAlumno().getNombreCompleto());
            }
            if (entity.getNota().getCurso() != null) {
                builder.cursoNombre(entity.getNota().getCurso().getNombre());
            }
            if (entity.getNota().getBimestre() != null) {
                builder.bimestreNumero(entity.getNota().getBimestre().getNumero());
            }
        }

        if (entity.getUsuario() != null) {
            builder.usuarioId(entity.getUsuario().getId())
                   .usuarioNombre(entity.getUsuario().getUsername());
        }

        return builder.build();
    }
}
