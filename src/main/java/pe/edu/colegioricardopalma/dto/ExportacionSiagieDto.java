package pe.edu.colegioricardopalma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.ExportacionSiagie;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportacionSiagieDto {

    private UUID id;
    private String tipo;
    private String periodo;
    private String archivoUrl;
    private UUID usuarioId;
    private String usuarioUsername;
    private LocalDateTime fecha;

    public static ExportacionSiagieDto fromEntity(ExportacionSiagie entity) {
        if (entity == null) return null;

        return ExportacionSiagieDto.builder()
                .id(entity.getId())
                .tipo(entity.getTipo())
                .periodo(entity.getPeriodo())
                .archivoUrl(entity.getArchivoUrl())
                .usuarioId(entity.getUsuario() != null ? entity.getUsuario().getId() : null)
                .usuarioUsername(entity.getUsuario() != null ? entity.getUsuario().getUsername() : null)
                .fecha(entity.getFecha() != null ? entity.getFecha() : entity.getCreatedAt())
                .build();
    }
}
