package pe.edu.colegioricardopalma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Estado de cuenta de un padre/apoderado.
 * Incluye todas las pensiones pendientes de sus hijos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoCuentaDto {

    private UUID apoderadoId;
    private String apoderadoNombres;
    private String apoderadoApellidos;
    private List<HijoEstadoCuenta> hijos;
    private BigDecimal totalPendiente;
    private BigDecimal totalPagado;
    private Integer cantidadPensionesPendientes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HijoEstadoCuenta {
        private UUID alumnoId;
        private String alumnoNombres;
        private String alumnoApellidos;
        private String alumnoCodigo;
        private String grado;
        private String seccion;
        private List<PensionDto> pensionesPendientes;
        private List<PensionDto> pensionesHistorial;
        private BigDecimal totalPendiente;
        private BigDecimal totalPagado;
    }
}
