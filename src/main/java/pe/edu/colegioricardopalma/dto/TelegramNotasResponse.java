package pe.edu.colegioricardopalma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramNotasResponse {

    private String mensaje;
    private List<AlumnoNotasResumen> alumnos;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AlumnoNotasResumen {
        private String alumnoId;
        private String alumnoNombre;
        private String grado;
        private String seccion;
        private Integer totalNotas;
        private String promedio;
    }
}
