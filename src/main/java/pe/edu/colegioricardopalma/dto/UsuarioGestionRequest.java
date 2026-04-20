package pe.edu.colegioricardopalma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioGestionRequest {
    private String tipo;
    private String username;
    private String email;
    private String password;
    private String dni;
    private String codigoEstudiante;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String direccion;
    private String especialidad;
    private LocalDate fechaNacimiento;
    private UUID gradoId;
    private UUID seccionId;
    private String estado;
    private Boolean crearHijo;
    private String hijoDni;
    private String hijoCodigoEstudiante;
    private String hijoNombres;
    private String hijoApellidos;
    private LocalDate hijoFechaNacimiento;
    private UUID hijoGradoId;
    private UUID hijoSeccionId;
}
