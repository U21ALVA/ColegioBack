package pe.edu.colegioricardopalma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.Nivel;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioGestionDto {
    private UUID id;
    private String tipo;
    private UUID usuarioId;
    private String username;
    private String email;
    private String dni;
    private String codigoEstudiante;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String direccion;
    private String especialidad;
    private LocalDate fechaNacimiento;
    private UUID gradoId;
    private String gradoNombre;
    private Nivel nivel;
    private UUID seccionId;
    private String seccionNombre;
    private Integer hijosCount;
    private String estado;
}
