package pe.edu.colegioricardopalma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.ConfiguracionPensionCreateRequest;
import pe.edu.colegioricardopalma.dto.ConfiguracionPensionDto;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.entity.AnioEscolar;
import pe.edu.colegioricardopalma.entity.ConfiguracionPension;
import pe.edu.colegioricardopalma.repository.AnioEscolarRepository;
import pe.edu.colegioricardopalma.repository.ConfiguracionPensionRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfiguracionPensionService {

    private final ConfiguracionPensionRepository configuracionRepository;
    private final AnioEscolarRepository anioEscolarRepository;

    public List<ConfiguracionPensionDto> findAll() {
        return configuracionRepository.findAll().stream()
                .map(ConfiguracionPensionDto::fromEntity)
                .collect(Collectors.toList());
    }

    public ConfiguracionPensionDto findById(UUID id) {
        ConfiguracionPension config = configuracionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Configuración no encontrada: " + id));
        return ConfiguracionPensionDto.fromEntity(config);
    }

    public ConfiguracionPensionDto findByAnioEscolarActivo() {
        return configuracionRepository.findByAnioEscolarActivo()
                .map(ConfiguracionPensionDto::fromEntity)
                .orElse(null);
    }

    public ConfiguracionPensionDto findByAnioEscolarId(UUID anioEscolarId) {
        return configuracionRepository.findByAnioEscolarIdWithDetails(anioEscolarId)
                .map(ConfiguracionPensionDto::fromEntity)
                .orElse(null);
    }

    @Transactional
    public ConfiguracionPensionDto create(ConfiguracionPensionCreateRequest request) {
        // Validate año escolar
        AnioEscolar anioEscolar = anioEscolarRepository.findById(request.getAnioEscolarId())
                .orElseThrow(() -> new EntityNotFoundException("Año escolar no encontrado: " + request.getAnioEscolarId()));

        // Check if configuration already exists for this year
        if (configuracionRepository.existsByAnioEscolarId(request.getAnioEscolarId())) {
            throw new IllegalArgumentException("Ya existe una configuración para este año escolar");
        }

        ConfiguracionPension config = ConfiguracionPension.builder()
                .anioEscolar(anioEscolar)
                .montoBase(request.getMontoBase())
                .fechaVencimientoDia(request.getFechaVencimientoDia())
                .porcentajeMora(request.getPorcentajeMora())
                .build();

        ConfiguracionPension saved = configuracionRepository.save(config);
        log.info("Configuración de pensión creada para año {}", anioEscolar.getAnio());

        return ConfiguracionPensionDto.fromEntity(saved);
    }

    @Transactional
    public ConfiguracionPensionDto update(UUID id, ConfiguracionPensionCreateRequest request) {
        ConfiguracionPension config = configuracionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Configuración no encontrada: " + id));

        config.setMontoBase(request.getMontoBase());
        config.setFechaVencimientoDia(request.getFechaVencimientoDia());
        config.setPorcentajeMora(request.getPorcentajeMora());

        ConfiguracionPension saved = configuracionRepository.save(config);
        log.info("Configuración de pensión actualizada: {}", id);

        return ConfiguracionPensionDto.fromEntity(saved);
    }

    @Transactional
    public void delete(UUID id) {
        if (!configuracionRepository.existsById(id)) {
            throw new EntityNotFoundException("Configuración no encontrada: " + id);
        }
        configuracionRepository.deleteById(id);
        log.info("Configuración de pensión eliminada: {}", id);
    }
}
