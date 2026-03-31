package pe.edu.colegioricardopalma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.ComunicadoEntrega;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComunicadoEntregaDto {

    private UUID id;
    private UUID apoderadoId;
    private String apoderadoNombre;
    private Long telegramMessageId;
    private Boolean entregado;
    private Boolean leido;
    private LocalDateTime fechaEntrega;
    private LocalDateTime fechaLectura;
    private String errorMensaje;

    public static ComunicadoEntregaDto fromEntity(ComunicadoEntrega entity) {
        if (entity == null) return null;
        return ComunicadoEntregaDto.builder()
                .id(entity.getId())
                .apoderadoId(entity.getApoderado() != null ? entity.getApoderado().getId() : null)
                .apoderadoNombre(entity.getApoderado() != null ? entity.getApoderado().getNombreCompleto() : null)
                .telegramMessageId(entity.getTelegramMessageId())
                .entregado(entity.getEntregado())
                .leido(entity.getLeido())
                .fechaEntrega(entity.getFechaEntrega())
                .fechaLectura(entity.getFechaLectura())
                .errorMensaje(entity.getErrorMensaje())
                .build();
    }
}
