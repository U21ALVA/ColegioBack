package pe.edu.colegioricardopalma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramDeudasResponse {

    private String mensaje;
    private List<AlumnoDeudasResumen> alumnos;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AlumnoDeudasResumen {
        private String alumnoId;
        private String alumnoNombre;
        private Integer totalPendientes;
        private String montoTotalPendiente;
        private List<DetalleDeuda> deudas;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetalleDeuda {
        private String pensionId;
        private String mes;
        private String estado;
        private String monto;
        private String vencimiento;
    }
}
