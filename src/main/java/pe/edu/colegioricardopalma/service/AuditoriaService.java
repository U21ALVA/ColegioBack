package pe.edu.colegioricardopalma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.entity.AuditoriaAcceso;
import pe.edu.colegioricardopalma.entity.Usuario;
import pe.edu.colegioricardopalma.repository.AuditoriaAccesoRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaAccesoRepository auditoriaAccesoRepository;

    public static final String ACCION_LOGIN = "LOGIN";
    public static final String ACCION_LOGOUT = "LOGOUT";
    public static final String ACCION_LOGIN_FALLIDO = "LOGIN_FALLIDO";
    public static final String ACCION_REFRESH_TOKEN = "REFRESH_TOKEN";

    @Transactional
    public void registrarAcceso(Usuario usuario, String accion, String ip, String userAgent) {
        try {
            AuditoriaAcceso auditoria = AuditoriaAcceso.builder()
                    .usuario(usuario)
                    .accion(accion)
                    .ip(ip)
                    .userAgent(userAgent)
                    .build();
            
            auditoriaAccesoRepository.save(auditoria);
            log.debug("Acceso registrado: {} - {} desde {}", 
                    usuario != null ? usuario.getUsername() : "unknown", accion, ip);
        } catch (Exception e) {
            log.error("Error registrando auditoría de acceso: {}", e.getMessage());
            // Don't throw - auditing shouldn't break the main flow
        }
    }

    @Transactional
    public void registrarLoginFallido(String username, String ip, String userAgent) {
        try {
            AuditoriaAcceso auditoria = AuditoriaAcceso.builder()
                    .usuario(null)
                    .accion(ACCION_LOGIN_FALLIDO + ":" + username)
                    .ip(ip)
                    .userAgent(userAgent)
                    .build();
            
            auditoriaAccesoRepository.save(auditoria);
            log.warn("Login fallido para usuario: {} desde IP: {}", username, ip);
        } catch (Exception e) {
            log.error("Error registrando login fallido: {}", e.getMessage());
        }
    }
}
