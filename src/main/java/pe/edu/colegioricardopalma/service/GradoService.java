package pe.edu.colegioricardopalma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.GradoDto;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.entity.Estado;
import pe.edu.colegioricardopalma.entity.Grado;
import pe.edu.colegioricardopalma.entity.Nivel;
import pe.edu.colegioricardopalma.repository.GradoRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradoService {

    private final GradoRepository gradoRepository;

    public List<GradoDto> findAll() {
        return gradoRepository.findAllOrderByOrden()
                .stream()
                .map(GradoDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<GradoDto> findAllActivos() {
        return gradoRepository.findAllActiveOrderByOrden(Estado.ACTIVO)
                .stream()
                .map(GradoDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<GradoDto> findByNivel(Nivel nivel) {
        return gradoRepository.findByNivelOrderByOrdenAsc(nivel)
                .stream()
                .map(GradoDto::fromEntity)
                .collect(Collectors.toList());
    }

    public PageResponse<GradoDto> findAllPaginated(Pageable pageable) {
        Page<GradoDto> page = gradoRepository.findByEstado(Estado.ACTIVO, pageable)
                .map(GradoDto::fromEntity);
        return PageResponse.from(page);
    }

    public GradoDto findById(UUID id) {
        Grado grado = gradoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Grado no encontrado: " + id));
        return GradoDto.fromEntity(grado);
    }

    public GradoDto findByIdWithSecciones(UUID id) {
        Grado grado = gradoRepository.findByIdWithSecciones(id)
                .orElseThrow(() -> new EntityNotFoundException("Grado no encontrado: " + id));
        return GradoDto.fromEntityWithSecciones(grado);
    }

    @Transactional
    public GradoDto create(GradoDto dto) {
        if (gradoRepository.existsByNombreAndNivel(dto.getNombre(), dto.getNivel())) {
            throw new IllegalArgumentException("Ya existe un grado con ese nombre y nivel");
        }

        Grado grado = dto.toEntity();
        Grado saved = gradoRepository.save(grado);
        log.info("Grado creado: {} ({})", saved.getNombre(), saved.getNivel());
        
        return GradoDto.fromEntity(saved);
    }

    @Transactional
    public GradoDto update(UUID id, GradoDto dto) {
        Grado grado = gradoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Grado no encontrado: " + id));

        // Check for duplicates if name or nivel changed
        if ((!grado.getNombre().equals(dto.getNombre()) || !grado.getNivel().equals(dto.getNivel())) 
                && gradoRepository.existsByNombreAndNivel(dto.getNombre(), dto.getNivel())) {
            throw new IllegalArgumentException("Ya existe un grado con ese nombre y nivel");
        }

        grado.setNombre(dto.getNombre());
        grado.setNivel(dto.getNivel());
        grado.setOrden(dto.getOrden());
        if (dto.getEstado() != null) {
            grado.setEstado(dto.getEstado());
        }
        
        Grado saved = gradoRepository.save(grado);
        log.info("Grado actualizado: {} ({})", saved.getNombre(), saved.getNivel());
        
        return GradoDto.fromEntity(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Grado grado = gradoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Grado no encontrado: " + id));

        // Soft delete - change status to ELIMINADO
        grado.setEstado(Estado.ELIMINADO);
        gradoRepository.save(grado);
        log.info("Grado eliminado (soft): {} ({})", grado.getNombre(), grado.getNivel());
    }
}
