package pe.edu.colegioricardopalma.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.AnioEscolar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnioEscolarDto {

    private UUID id;
    
    @NotNull(message = "El año es requerido")
    private Integer anio;
    
    @NotNull(message = "La fecha de inicio es requerida")
    private LocalDate fechaInicio;
    
    @NotNull(message = "La fecha de fin es requerida")
    private LocalDate fechaFin;
    
    private Boolean activo;
    private LocalDateTime createdAt;

    public static AnioEscolarDto fromEntity(AnioEscolar entity) {
        return AnioEscolarDto.builder()
                .id(entity.getId())
                .anio(entity.getAnio())
                .fechaInicio(entity.getFechaInicio())
                .fechaFin(entity.getFechaFin())
                .activo(entity.getActivo())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public AnioEscolar toEntity() {
        return AnioEscolar.builder()
                .id(this.id)
                .anio(this.anio)
                .fechaInicio(this.fechaInicio)
                .fechaFin(this.fechaFin)
                .activo(this.activo != null ? this.activo : false)
                .build();
    }

    public String getNombre() {
        return anio != null ? String.valueOf(anio) : null;
    }
}
