package pe.edu.colegioricardopalma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.DocenteCursoDto;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.entity.*;
import pe.edu.colegioricardopalma.repository.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocenteCursoService {

    private final DocenteCursoRepository docenteCursoRepository;
    private final DocenteRepository docenteRepository;
    private final CursoRepository cursoRepository;
    private final GradoRepository gradoRepository;
    private final SeccionRepository seccionRepository;
    private final AnioEscolarRepository anioEscolarRepository;

    public List<DocenteCursoDto> findByAnioEscolar(UUID anioEscolarId) {
        return docenteCursoRepository.findAllByAnioEscolarWithDetails(anioEscolarId, Estado.ACTIVO)
                .stream()
                .map(DocenteCursoDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<DocenteCursoDto> findByDocenteAndAnioEscolar(UUID docenteId, UUID anioEscolarId) {
        return docenteCursoRepository.findByDocenteAndAnioEscolarWithDetails(docenteId, anioEscolarId, Estado.ACTIVO)
                .stream()
                .map(DocenteCursoDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<DocenteCursoDto> findByGradoSeccionAndAnioEscolar(UUID gradoId, UUID seccionId, UUID anioEscolarId) {
        return docenteCursoRepository.findByGradoIdAndSeccionIdAndAnioEscolarId(gradoId, seccionId, anioEscolarId)
                .stream()
                .map(DocenteCursoDto::fromEntity)
                .collect(Collectors.toList());
    }

    public PageResponse<DocenteCursoDto> findAllPaginated(UUID anioEscolarId, Pageable pageable) {
        Page<DocenteCursoDto> page = docenteCursoRepository.findByAnioEscolarIdAndEstado(anioEscolarId, Estado.ACTIVO, pageable)
                .map(DocenteCursoDto::fromEntity);
        return PageResponse.from(page);
    }

    public DocenteCursoDto findById(UUID id) {
        DocenteCurso docenteCurso = docenteCursoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asignación no encontrada: " + id));
        return DocenteCursoDto.fromEntity(docenteCurso);
    }

    @Transactional
    public DocenteCursoDto create(DocenteCursoDto dto) {
        // Validate all entities exist
        Docente docente = docenteRepository.findById(dto.getDocenteId())
                .orElseThrow(() -> new EntityNotFoundException("Docente no encontrado: " + dto.getDocenteId()));

        Curso curso = cursoRepository.findById(dto.getCursoId())
                .orElseThrow(() -> new EntityNotFoundException("Curso no encontrado: " + dto.getCursoId()));

        Grado grado = gradoRepository.findById(dto.getGradoId())
                .orElseThrow(() -> new EntityNotFoundException("Grado no encontrado: " + dto.getGradoId()));

        Seccion seccion = seccionRepository.findById(dto.getSeccionId())
                .orElseThrow(() -> new EntityNotFoundException("Sección no encontrada: " + dto.getSeccionId()));

        AnioEscolar anioEscolar = anioEscolarRepository.findById(dto.getAnioEscolarId())
                .orElseThrow(() -> new EntityNotFoundException("Año escolar no encontrado: " + dto.getAnioEscolarId()));

        // Check for duplicate assignment
        if (docenteCursoRepository.existsByDocenteIdAndCursoIdAndGradoIdAndSeccionIdAndAnioEscolarId(
                dto.getDocenteId(), dto.getCursoId(), dto.getGradoId(), dto.getSeccionId(), dto.getAnioEscolarId())) {
            throw new IllegalArgumentException("Ya existe esta asignación para el año escolar actual");
        }

        // Validate curso level matches grado level
        if (!curso.getNivel().equals(grado.getNivel())) {
            throw new IllegalArgumentException("El nivel del curso no coincide con el nivel del grado");
        }

        DocenteCurso docenteCurso = DocenteCurso.builder()
                .docente(docente)
                .curso(curso)
                .grado(grado)
                .seccion(seccion)
                .anioEscolar(anioEscolar)
                .estado(Estado.ACTIVO)
                .build();

        DocenteCurso saved = docenteCursoRepository.save(docenteCurso);
        log.info("Asignación creada: {} -> {} en {} {}", 
                docente.getNombreCompleto(), curso.getNombre(), grado.getNombre(), seccion.getNombre());
        
        return DocenteCursoDto.fromEntity(saved);
    }

    @Transactional
    public DocenteCursoDto update(UUID id, DocenteCursoDto dto) {
        DocenteCurso docenteCurso = docenteCursoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asignación no encontrada: " + id));

        // For now, only allow changing the docente
        if (dto.getDocenteId() != null && !dto.getDocenteId().equals(docenteCurso.getDocente().getId())) {
            Docente newDocente = docenteRepository.findById(dto.getDocenteId())
                    .orElseThrow(() -> new EntityNotFoundException("Docente no encontrado: " + dto.getDocenteId()));
            docenteCurso.setDocente(newDocente);
        }

        if (dto.getEstado() != null) {
            docenteCurso.setEstado(dto.getEstado());
        }
        
        DocenteCurso saved = docenteCursoRepository.save(docenteCurso);
        log.info("Asignación actualizada: {}", saved.getId());
        
        return DocenteCursoDto.fromEntity(saved);
    }

    @Transactional
    public void delete(UUID id) {
        DocenteCurso docenteCurso = docenteCursoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asignación no encontrada: " + id));

        docenteCurso.setEstado(Estado.ELIMINADO);
        docenteCursoRepository.save(docenteCurso);
        log.info("Asignación eliminada (soft): {}", id);
    }
}
