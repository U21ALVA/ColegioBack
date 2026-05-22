package pe.edu.colegioricardopalma.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PagoManualRequest(
        @NotNull(message = "La pensión es requerida") UUID pensionId,
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0") BigDecimal monto,
        @NotBlank(message = "El método de pago es requerido") String metodoPago
) {}
