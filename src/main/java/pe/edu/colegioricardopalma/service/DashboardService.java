package pe.edu.colegioricardopalma.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pe.edu.colegioricardopalma.dto.ActividadRecienteDto;
import pe.edu.colegioricardopalma.entity.AuditoriaAcceso;
import pe.edu.colegioricardopalma.entity.Pago;
import pe.edu.colegioricardopalma.repository.AuditoriaAccesoRepository;
import pe.edu.colegioricardopalma.repository.PagoRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DashboardService {

    private final AuditoriaAccesoRepository auditoriaAccesoRepository;
    private final PagoRepository pagoRepository;

    public DashboardService(AuditoriaAccesoRepository auditoriaAccesoRepository, PagoRepository pagoRepository) {
        this.auditoriaAccesoRepository = auditoriaAccesoRepository;
        this.pagoRepository = pagoRepository;
    }

    public List<ActividadRecienteDto> getActividadReciente(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 30));

        List<ActividadRecienteDto> actividades = new ArrayList<>();

        List<AuditoriaAcceso> accesos = auditoriaAccesoRepository.findRecent(PageRequest.of(0, safeLimit));
        for (AuditoriaAcceso acceso : accesos) {
            String username = acceso.getUsuario() != null ? acceso.getUsuario().getUsername() : "desconocido";
            actividades.add(new ActividadRecienteDto(
                    "acceso",
                    mapAccionTitulo(acceso.getAccion()),
                    "Usuario " + username,
                    acceso.getCreatedAt()
            ));
        }

        List<Pago> pagos = pagoRepository.findRecentCompleted(PageRequest.of(0, safeLimit));
        for (Pago pago : pagos) {
            String alumno = pago.getPension().getAlumno().getApellidos() + ", " + pago.getPension().getAlumno().getNombres();
            String metodo = mapMetodoLabel(pago.getMetodoPago());

            actividades.add(new ActividadRecienteDto(
                    "pago",
                    "Pago recibido",
                    alumno + " · S/. " + pago.getMonto() + " · " + metodo,
                    pago.getFechaPago() != null ? pago.getFechaPago() : pago.getCreatedAt()
            ));
        }

        return actividades.stream()
                .filter(a -> a.fecha() != null)
                .sorted(Comparator.comparing(ActividadRecienteDto::fecha, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                .limit(safeLimit)
                .toList();
    }

    private String mapAccionTitulo(String accion) {
        if (accion == null) return "Acción de usuario";

        if (accion.startsWith(AuditoriaService.ACCION_LOGIN_FALLIDO)) {
            return "Intento de login fallido";
        }

        return switch (accion) {
            case AuditoriaService.ACCION_LOGIN -> "Inicio de sesión";
            case AuditoriaService.ACCION_LOGOUT -> "Cierre de sesión";
            case AuditoriaService.ACCION_REFRESH_TOKEN -> "Renovación de sesión";
            default -> "Acción de usuario";
        };
    }

    private String mapMetodoLabel(String metodoPago) {
        String metodo = metodoPago == null ? "" : metodoPago.trim().toLowerCase();
        return switch (metodo) {
            case "card", "tarjeta", "stripe" -> "Tarjeta";
            case "efectivo", "cash" -> "Efectivo";
            default -> metodoPago == null || metodoPago.isBlank() ? "Sin método" : metodoPago;
        };
    }
}
