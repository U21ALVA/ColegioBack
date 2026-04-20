package pe.edu.colegioricardopalma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.colegioricardopalma.entity.ComunicadoDestino;
import pe.edu.colegioricardopalma.entity.Nivel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComunicadoCreateRequest {

    @NotBlank(message = "El título es requerido")
    private String titulo;

    @NotBlank(message = "El contenido es requerido")
    private String contenido;

    private String adjuntoUrl;

    @NotNull(message = "El tipo de destino es requerido")
    private ComunicadoDestino destinoTipo;

    private List<UUID> destinoIds;

    // Usado cuando destinoTipo = NIVEL. Se resolverá a grados al guardar.
    private List<Nivel> niveles;

    private Boolean esReunion;
    private LocalDateTime fechaReunionInicio;
    private LocalDateTime fechaReunionFin;
    private String lugarReunion;
}
