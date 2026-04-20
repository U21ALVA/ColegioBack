package pe.edu.colegioricardopalma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
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
    private Boolean esReunion;
    private LocalDateTime fechaReunionInicio;
    private LocalDateTime fechaReunionFin;
    private String lugarReunion;
    private UUID createdBy;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ComunicadoDto fromEntity(Comunicado entity) {
        if (entity == null) return null;

        UUID createdById = null;
        String createdByUsername = null;
        if (entity.getCreatedBy() != null && Hibernate.isInitialized(entity.getCreatedBy())) {
            createdById = entity.getCreatedBy().getId();
            createdByUsername = entity.getCreatedBy().getUsername();
        }

        return ComunicadoDto.builder()
                .id(entity.getId())
                .titulo(entity.getTitulo())
                .contenido(entity.getContenido())
                .adjuntoUrl(entity.getAdjuntoUrl())
                .destinoTipo(entity.getDestinoTipo())
                .destinoIds(entity.getDestinoIds() != null ? Arrays.asList(entity.getDestinoIds()) : List.of())
                .estado(entity.getEstado())
                .fechaPublicacion(entity.getFechaPublicacion())
                .esReunion(entity.getEsReunion())
                .fechaReunionInicio(entity.getFechaReunionInicio())
                .fechaReunionFin(entity.getFechaReunionFin())
                .lugarReunion(entity.getLugarReunion())
                .createdBy(createdById)
                .createdByUsername(createdByUsername)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
