package pe.edu.colegioricardopalma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.AnioEscolarDto;
import pe.edu.colegioricardopalma.entity.AnioEscolar;
import pe.edu.colegioricardopalma.repository.AnioEscolarRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnioEscolarService {

    private final AnioEscolarRepository anioEscolarRepository;

    public List<AnioEscolarDto> findAll() {
        return anioEscolarRepository.findAllOrderByAnioDesc()
                .stream()
                .map(AnioEscolarDto::fromEntity)
                .collect(Collectors.toList());
    }

    public AnioEscolarDto findById(UUID id) {
        AnioEscolar anioEscolar = anioEscolarRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Año escolar no encontrado: " + id));
        return AnioEscolarDto.fromEntity(anioEscolar);
    }

    public AnioEscolarDto findActivo() {
        AnioEscolar anioEscolar = anioEscolarRepository.findByActivoTrue()
                .orElseThrow(() -> new EntityNotFoundException("No hay año escolar activo"));
        return AnioEscolarDto.fromEntity(anioEscolar);
    }

    @Transactional
    public AnioEscolarDto create(AnioEscolarDto dto) {
        if (anioEscolarRepository.existsByAnio(dto.getAnio())) {
            throw new IllegalArgumentException("Ya existe un año escolar para el año: " + dto.getAnio());
        }

        AnioEscolar anioEscolar = dto.toEntity();
        anioEscolar.setActivo(false); // New years are inactive by default
        
        AnioEscolar saved = anioEscolarRepository.save(anioEscolar);
        log.info("Año escolar creado: {}", saved.getAnio());
        
        return AnioEscolarDto.fromEntity(saved);
    }

    @Transactional
    public AnioEscolarDto update(UUID id, AnioEscolarDto dto) {
        AnioEscolar anioEscolar = anioEscolarRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Año escolar no encontrado: " + id));

        // Check if changing the year would cause a duplicate
        if (!anioEscolar.getAnio().equals(dto.getAnio()) && anioEscolarRepository.existsByAnio(dto.getAnio())) {
            throw new IllegalArgumentException("Ya existe un año escolar para el año: " + dto.getAnio());
        }

        anioEscolar.setAnio(dto.getAnio());
        anioEscolar.setFechaInicio(dto.getFechaInicio());
        anioEscolar.setFechaFin(dto.getFechaFin());
        
        AnioEscolar saved = anioEscolarRepository.save(anioEscolar);
        log.info("Año escolar actualizado: {}", saved.getAnio());
        
        return AnioEscolarDto.fromEntity(saved);
    }

    @Transactional
    public AnioEscolarDto activate(UUID id) {
        AnioEscolar anioEscolar = anioEscolarRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Año escolar no encontrado: " + id));

        // Deactivate current active year
        anioEscolarRepository.findByActivoTrue().ifPresent(current -> {
            current.setActivo(false);
            anioEscolarRepository.save(current);
            log.info("Año escolar desactivado: {}", current.getAnio());
        });

        // Activate the selected year
        anioEscolar.setActivo(true);
        AnioEscolar saved = anioEscolarRepository.save(anioEscolar);
        log.info("Año escolar activado: {}", saved.getAnio());
        
        return AnioEscolarDto.fromEntity(saved);
    }

    @Transactional
    public void delete(UUID id) {
        AnioEscolar anioEscolar = anioEscolarRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Año escolar no encontrado: " + id));

        if (anioEscolar.getActivo()) {
            throw new IllegalStateException("No se puede eliminar el año escolar activo");
        }

        anioEscolarRepository.delete(anioEscolar);
        log.info("Año escolar eliminado: {}", anioEscolar.getAnio());
    }
}
