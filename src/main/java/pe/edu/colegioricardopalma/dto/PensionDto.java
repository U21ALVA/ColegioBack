package pe.edu.colegioricardopalma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.Pension;
import pe.edu.colegioricardopalma.entity.PensionEstado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PensionDto {

    private UUID id;
    private UUID alumnoId;
    private String alumnoNombres;
    private String alumnoApellidos;
    private String alumnoCodigo;
    private String alumnoGrado;
    private String alumnoSeccion;
    private UUID anioEscolarId;
    private Integer anioEscolar;
    private Integer mes;
    private String nombreMes;
    private BigDecimal monto;
    private BigDecimal descuento;
    private BigDecimal montoFinal;
    private PensionEstado estado;
    private LocalDate fechaVencimiento;

    public static PensionDto fromEntity(Pension entity) {
        if (entity == null) return null;

        var alumno = entity.getAlumno();
        var anioEscolar = entity.getAnioEscolar();

        return PensionDto.builder()
                .id(entity.getId())
                .alumnoId(alumno != null ? alumno.getId() : null)
                .alumnoNombres(alumno != null ? alumno.getNombres() : null)
                .alumnoApellidos(alumno != null ? alumno.getApellidos() : null)
                .alumnoCodigo(alumno != null ? alumno.getCodigoEstudiante() : null)
                .alumnoGrado(alumno != null && alumno.getGrado() != null ? alumno.getGrado().getNombre() : null)
                .alumnoSeccion(alumno != null && alumno.getSeccion() != null ? alumno.getSeccion().getNombre() : null)
                .anioEscolarId(anioEscolar != null ? anioEscolar.getId() : null)
                .anioEscolar(anioEscolar != null ? anioEscolar.getAnio() : null)
                .mes(entity.getMes())
                .nombreMes(entity.getNombreMes())
                .monto(entity.getMonto())
                .descuento(entity.getDescuento())
                .montoFinal(entity.getMontoFinal())
                .estado(entity.getEstado())
                .fechaVencimiento(entity.getFechaVencimiento())
                .build();
    }
}
