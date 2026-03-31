package pe.edu.colegioricardopalma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramVincularRequest {

    @NotBlank(message = "El código es requerido")
    private String codigo;

    @NotNull(message = "El chatId es requerido")
    private Long chatId;
}
