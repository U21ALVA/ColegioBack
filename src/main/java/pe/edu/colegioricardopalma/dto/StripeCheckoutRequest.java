package pe.edu.colegioricardopalma.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StripeCheckoutRequest {

    @NotNull(message = "El ID de la pensión es requerido")
    private UUID pensionId;

    private String successUrl;
    private String cancelUrl;
}
