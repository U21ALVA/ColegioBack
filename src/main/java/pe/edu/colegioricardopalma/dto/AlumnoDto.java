package pe.edu.colegioricardopalma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.Alumno;
import pe.edu.colegioricardopalma.entity.Estado;
import pe.edu.colegioricardopalma.entity.Nivel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlumnoDto {

    private UUID id;
    
    @NotBlank(message = "El DNI es requerido")
    @Size(min = 8, max = 15, message = "El DNI debe tener entre 8 y 15 caracteres")
    private String dni;
    
    @Size(max = 20, message = "El código de estudiante no puede exceder 20 caracteres")
    private String codigoEstudiante;
    
    @NotBlank(message = "Los nombres son requeridos")
    @Size(max = 100, message = "Los nombres no pueden exceder 100 caracteres")
    private String nombres;
    
    @NotBlank(message = "Los apellidos son requeridos")
    @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres")
    private String apellidos;
    
    private LocalDate fechaNacimiento;
    
    private UUID gradoId;
    private String gradoNombre;
    private Nivel nivel;
    
    private UUID seccionId;
    private String seccionNombre;
    
    private Estado estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Related apoderados
    private List<AlumnoApoderadoDto> apoderados;

    public static AlumnoDto fromEntity(Alumno entity) {
        AlumnoDto dto = AlumnoDto.builder()
                .id(entity.getId())
                .dni(entity.getDni())
                .codigoEstudiante(entity.getCodigoEstudiante())
                .nombres(entity.getNombres())
                .apellidos(entity.getApellidos())
                .fechaNacimiento(entity.getFechaNacimiento())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
        
        if (entity.getGrado() != null) {
            dto.setGradoId(entity.getGrado().getId());
            dto.setGradoNombre(entity.getGrado().getNombre());
            dto.setNivel(entity.getGrado().getNivel());
        }
        
        if (entity.getSeccion() != null) {
            dto.setSeccionId(entity.getSeccion().getId());
            dto.setSeccionNombre(entity.getSeccion().getNombre());
        }
        
        return dto;
    }

    public Alumno toEntity() {
        return Alumno.builder()
                .id(this.id)
                .dni(this.dni)
                .codigoEstudiante(this.codigoEstudiante)
                .nombres(this.nombres)
                .apellidos(this.apellidos)
                .fechaNacimiento(this.fechaNacimiento)
                .estado(this.estado != null ? this.estado : Estado.ACTIVO)
                .build();
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}
