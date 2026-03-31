package pe.edu.colegioricardopalma.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.ConfiguracionSiagieDto;
import pe.edu.colegioricardopalma.entity.ConfiguracionSiagie;
import pe.edu.colegioricardopalma.entity.Usuario;
import pe.edu.colegioricardopalma.repository.ConfiguracionSiagieRepository;
import pe.edu.colegioricardopalma.repository.UsuarioRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiagieConfigService {

    private final ConfiguracionSiagieRepository configuracionSiagieRepository;
    private final UsuarioRepository usuarioRepository;

    public ConfiguracionSiagieDto getLatestConfig() {
        return configuracionSiagieRepository.findTopByOrderByUpdatedAtDesc()
                .map(ConfiguracionSiagieDto::fromEntity)
                .orElse(null);
    }

    @Transactional
    public ConfiguracionSiagieDto upsert(ConfiguracionSiagieDto request, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + username));

        ConfiguracionSiagie config = configuracionSiagieRepository
                .findTopByOrderByUpdatedAtDesc()
                .orElseGet(ConfiguracionSiagie::new);

        config.setInstitucionEducativa(request.getInstitucionEducativa());
        config.setCodigoModularAnexo(request.getCodigoModularAnexo());
        config.setNivel(request.getNivel());
        config.setNombreReporte(request.getNombreReporte());
        config.setAnioAcademico(request.getAnioAcademico());
        config.setDisenoModular(request.getDisenoModular());
        config.setPeriodo(request.getPeriodo());
        config.setGrado(request.getGrado());
        config.setSeccion(request.getSeccion());
        config.setAreasCursos(request.getAreasCursos());

        if (config.getCreatedBy() == null) {
            config.setCreatedBy(usuario);
        }

        ConfiguracionSiagie saved = configuracionSiagieRepository.save(config);
        log.info("Configuración SIAGIE guardada por {}", username);
        return ConfiguracionSiagieDto.fromEntity(saved);
    }
}
