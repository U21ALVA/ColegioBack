package pe.edu.colegioricardopalma.dto;

import java.time.LocalDateTime;

public record ActividadRecienteDto(
        String tipo,
        String titulo,
        String descripcion,
        LocalDateTime fecha
) {}
