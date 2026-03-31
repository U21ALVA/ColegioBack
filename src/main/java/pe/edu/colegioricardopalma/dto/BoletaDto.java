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
 * Student report card (Boleta de Notas).
 * Contains all grades for a student organized by bimester.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoletaDto {

    private UUID alumnoId;
    private String alumnoNombres;
    private String alumnoApellidos;
    private String alumnoCodigo;
    private String gradoNombre;
    private String seccionNombre;
    
    private UUID anioEscolarId;
    private Integer anioEscolar;
    
    private List<CursoNotasDto> cursos;
    
    private BigDecimal promedioGeneral;
    private LiteralNota literalGeneral;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CursoNotasDto {
        private UUID cursoId;
        private String cursoNombre;
        private List<BimestreNotaDto> bimestres;
        private BigDecimal promedioAnual;
        private LiteralNota literalAnual;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BimestreNotaDto {
        private UUID bimestreId;
        private Integer numero;
        private BigDecimal n1;
        private BigDecimal n2;
        private BigDecimal n3;
        private BigDecimal n4;
        private BigDecimal notaFinal;
        private LiteralNota literal;
    }

    public String getAlumnoNombreCompleto() {
        if (alumnoNombres == null || alumnoApellidos == null) {
            return null;
        }
        return alumnoNombres + " " + alumnoApellidos;
    }
}
