package pe.edu.colegioricardopalma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.TelegramVinculacion;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramVinculacionDto {

    private Boolean verificado;
    private Long telegramChatId;
    private String codigoVerificacion;
    private LocalDateTime codigoExpiraAt;
    private LocalDateTime fechaVinculacion;

    public static TelegramVinculacionDto fromEntity(TelegramVinculacion entity) {
        if (entity == null) {
            return TelegramVinculacionDto.builder()
                    .verificado(false)
                    .build();
        }
        return TelegramVinculacionDto.builder()
                .verificado(entity.getVerificado())
                .telegramChatId(entity.getTelegramChatId())
                .codigoVerificacion(entity.getCodigoVerificacion())
                .codigoExpiraAt(entity.getCodigoExpiraAt())
                .fechaVinculacion(entity.getFechaVinculacion())
                .build();
    }
}
