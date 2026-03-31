package pe.edu.colegioricardopalma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.SeccionDto;
import pe.edu.colegioricardopalma.entity.Estado;
import pe.edu.colegioricardopalma.entity.Grado;
import pe.edu.colegioricardopalma.entity.Seccion;
import pe.edu.colegioricardopalma.repository.GradoRepository;
import pe.edu.colegioricardopalma.repository.SeccionRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeccionService {

    private final SeccionRepository seccionRepository;
    private final GradoRepository gradoRepository;

    public List<SeccionDto> findAll() {
        return seccionRepository.findAllActiveWithGrado(Estado.ACTIVO)
                .stream()
                .map(this::toDtoWithCount)
                .collect(Collectors.toList());
    }

    public List<SeccionDto> findByGradoId(UUID gradoId) {
        return seccionRepository.findByGradoIdOrderByNombreAsc(gradoId)
                .stream()
                .map(this::toDtoWithCount)
                .collect(Collectors.toList());
    }

    public List<SeccionDto> findByGradoIdActivas(UUID gradoId) {
        return seccionRepository.findByGradoIdAndEstado(gradoId, Estado.ACTIVO)
                .stream()
                .map(this::toDtoWithCount)
                .collect(Collectors.toList());
    }

    public SeccionDto findById(UUID id) {
        Seccion seccion = seccionRepository.findByIdWithGrado(id)
                .orElseThrow(() -> new EntityNotFoundException("Sección no encontrada: " + id));
        return toDtoWithCount(seccion);
    }

    @Transactional
    public SeccionDto create(SeccionDto dto) {
        Grado grado = gradoRepository.findById(dto.getGradoId())
                .orElseThrow(() -> new EntityNotFoundException("Grado no encontrado: " + dto.getGradoId()));

        if (seccionRepository.existsByNombreAndGradoId(dto.getNombre(), grado.getId())) {
            throw new IllegalArgumentException("Ya existe una sección con ese nombre para el grado seleccionado");
        }

        Seccion seccion = Seccion.builder()
                .nombre(dto.getNombre())
                .grado(grado)
                .capacidad(dto.getCapacidad() != null ? dto.getCapacidad() : 30)
                .estado(Estado.ACTIVO)
                .build();

        Seccion saved = seccionRepository.save(seccion);
        log.info("Sección creada: {} para grado {}", saved.getNombre(), grado.getNombre());
        
        return toDtoWithCount(saved);
    }

    @Transactional
    public SeccionDto update(UUID id, SeccionDto dto) {
        Seccion seccion = seccionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sección no encontrada: " + id));

        // Check for duplicate if name changed
        if (!seccion.getNombre().equals(dto.getNombre()) 
                && seccionRepository.existsByNombreAndGradoId(dto.getNombre(), seccion.getGrado().getId())) {
            throw new IllegalArgumentException("Ya existe una sección con ese nombre para el grado");
        }

        seccion.setNombre(dto.getNombre());
        if (dto.getCapacidad() != null) {
            seccion.setCapacidad(dto.getCapacidad());
        }
        if (dto.getEstado() != null) {
            seccion.setEstado(dto.getEstado());
        }
        
        Seccion saved = seccionRepository.save(seccion);
        log.info("Sección actualizada: {}", saved.getNombre());
        
        return toDtoWithCount(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Seccion seccion = seccionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sección no encontrada: " + id));

        Long alumnosActivos = seccionRepository.countAlumnosActivos(id);
        if (alumnosActivos > 0) {
            throw new IllegalStateException("No se puede eliminar la sección porque tiene " + alumnosActivos + " alumnos activos");
        }

        seccion.setEstado(Estado.ELIMINADO);
        seccionRepository.save(seccion);
        log.info("Sección eliminada (soft): {}", seccion.getNombre());
    }

    private SeccionDto toDtoWithCount(Seccion seccion) {
        SeccionDto dto = SeccionDto.fromEntity(seccion);
        dto.setAlumnosActivos(seccionRepository.countAlumnosActivos(seccion.getId()));
        return dto;
    }
}
