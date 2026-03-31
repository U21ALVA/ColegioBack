package pe.edu.colegioricardopalma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.Comunicado;
import pe.edu.colegioricardopalma.entity.ComunicadoDestino;
import pe.edu.colegioricardopalma.entity.ComunicadoEstado;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComunicadoDto {

    private UUID id;
    private String titulo;
    private String contenido;
    private String adjuntoUrl;
    private ComunicadoDestino destinoTipo;
    private List<UUID> destinoIds;
    private ComunicadoEstado estado;
    private LocalDateTime fechaPublicacion;
    private UUID createdBy;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ComunicadoDto fromEntity(Comunicado entity) {
        if (entity == null) return null;
        return ComunicadoDto.builder()
                .id(entity.getId())
                .titulo(entity.getTitulo())
                .contenido(entity.getContenido())
                .adjuntoUrl(entity.getAdjuntoUrl())
                .destinoTipo(entity.getDestinoTipo())
                .destinoIds(entity.getDestinoIds() != null ? Arrays.asList(entity.getDestinoIds()) : List.of())
                .estado(entity.getEstado())
                .fechaPublicacion(entity.getFechaPublicacion())
                .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)
                .createdByUsername(entity.getCreatedBy() != null ? entity.getCreatedBy().getUsername() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
