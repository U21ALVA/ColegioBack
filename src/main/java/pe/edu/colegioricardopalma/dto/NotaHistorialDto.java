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
    private String alumnoNombres;
    private String alumnoApellidos;
    private String alumnoCodigo;
    private String alumnoNombre;
    private String cursoNombre;
    private Integer bimestreNumero;
    
    private String campo;
    private String campoModificado;
    private String valorAnterior;
    private String valorNuevo;
    private String motivo;
    
    private UUID usuarioId;
    private String usuarioUsername;
    private String usuarioNombre;
    private String docenteNombres;
    private String docenteApellidos;
    
    private LocalDateTime createdAt;

    public static NotaHistorialDto fromEntity(NotaHistorial entity) {
        NotaHistorialDtoBuilder builder = NotaHistorialDto.builder()
                .id(entity.getId())
                .campo(entity.getCampoModificado())
                .campoModificado(entity.getCampoModificado())
                .valorAnterior(entity.getValorAnterior())
                .valorNuevo(entity.getValorNuevo())
                .motivo(entity.getMotivo())
                .createdAt(entity.getCreatedAt());

        if (entity.getNota() != null) {
            builder.notaId(entity.getNota().getId());
            if (entity.getNota().getAlumno() != null) {
                builder.alumnoNombres(entity.getNota().getAlumno().getNombres())
                        .alumnoApellidos(entity.getNota().getAlumno().getApellidos())
                        .alumnoCodigo(entity.getNota().getAlumno().getCodigoEstudiante());
                builder.alumnoNombre(entity.getNota().getAlumno().getNombreCompleto());
            }
            if (entity.getNota().getCurso() != null) {
                builder.cursoNombre(entity.getNota().getCurso().getNombre());
            }
            if (entity.getNota().getBimestre() != null) {
                builder.bimestreNumero(entity.getNota().getBimestre().getNumero());
            }
            if (entity.getNota().getDocente() != null) {
                builder.docenteNombres(entity.getNota().getDocente().getNombres())
                        .docenteApellidos(entity.getNota().getDocente().getApellidos());
            }
        }

        if (entity.getUsuario() != null) {
            builder.usuarioId(entity.getUsuario().getId())
                   .usuarioUsername(entity.getUsuario().getUsername())
                   .usuarioNombre(entity.getUsuario().getUsername());
        }

        return builder.build();
    }
}
