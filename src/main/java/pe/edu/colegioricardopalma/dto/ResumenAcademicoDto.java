package pe.edu.colegioricardopalma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.LiteralNota;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Academic summary for parent view.
 * Quick overview of a child's academic performance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenAcademicoDto {

    private UUID alumnoId;
    private String alumnoNombres;
    private String alumnoApellidos;
    private String alumnoCodigo;
    private String gradoNombre;
    private String seccionNombre;
    
    private Integer anioEscolar;
    private Integer bimestreActual;
    
    private BigDecimal promedioGeneral;
    private LiteralNota literalGeneral;
    
    private int totalCursos;
    private int cursosAprobados;
    private int cursosDesaprobados;
    
    private List<CursoResumenDto> cursos;
    private List<AlertaDto> alertas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CursoResumenDto {
        private UUID cursoId;
        private String cursoNombre;
        private BigDecimal ultimaNota;
        private LiteralNota literal;
        private String tendencia; // SUBIENDO, BAJANDO, ESTABLE
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertaDto {
        private String tipo; // BAJO_RENDIMIENTO, RECUPERACION_PENDIENTE, FELICITACION
        private String mensaje;
        private String cursoNombre;
    }

    public String getAlumnoNombreCompleto() {
        if (alumnoNombres == null || alumnoApellidos == null) {
            return null;
        }
        return alumnoNombres + " " + alumnoApellidos;
    }
}
