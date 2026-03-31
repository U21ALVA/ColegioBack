package pe.edu.colegioricardopalma.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.Docente;
import pe.edu.colegioricardopalma.entity.Estado;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocenteDto {

    private UUID id;
    
    @NotBlank(message = "El DNI es requerido")
    @Size(min = 8, max = 15, message = "El DNI debe tener entre 8 y 15 caracteres")
    private String dni;
    
    @NotBlank(message = "Los nombres son requeridos")
    @Size(max = 100, message = "Los nombres no pueden exceder 100 caracteres")
    private String nombres;
    
    @NotBlank(message = "Los apellidos son requeridos")
    @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres")
    private String apellidos;
    
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;
    
    @Email(message = "El email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;
    
    @Size(max = 100, message = "La especialidad no puede exceder 100 caracteres")
    private String especialidad;
    
    private UUID usuarioId;
    private String username;
    private Estado estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // For creating a user alongside the docente
    private Boolean crearUsuario;
    private String password;

    public static DocenteDto fromEntity(Docente entity) {
        DocenteDto dto = DocenteDto.builder()
                .id(entity.getId())
                .dni(entity.getDni())
                .nombres(entity.getNombres())
                .apellidos(entity.getApellidos())
                .telefono(entity.getTelefono())
                .email(entity.getEmail())
                .especialidad(entity.getEspecialidad())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
        
        if (entity.getUsuario() != null) {
            dto.setUsuarioId(entity.getUsuario().getId());
            dto.setUsername(entity.getUsuario().getUsername());
        }
        
        return dto;
    }

    public Docente toEntity() {
        return Docente.builder()
                .id(this.id)
                .dni(this.dni)
                .nombres(this.nombres)
                .apellidos(this.apellidos)
                .telefono(this.telefono)
                .email(this.email)
                .especialidad(this.especialidad)
                .estado(this.estado != null ? this.estado : Estado.ACTIVO)
                .build();
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}
