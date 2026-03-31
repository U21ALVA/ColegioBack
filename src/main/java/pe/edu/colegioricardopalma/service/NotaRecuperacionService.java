package pe.edu.colegioricardopalma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.NotaRecuperacionCreateRequest;
import pe.edu.colegioricardopalma.dto.NotaRecuperacionDto;
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
public class NotaRecuperacionService {

    private final NotaRecuperacionRepository notaRecuperacionRepository;
    private final AlumnoRepository alumnoRepository;
    private final CursoRepository cursoRepository;
    private final AnioEscolarRepository anioEscolarRepository;
    private final DocenteRepository docenteRepository;

    public List<NotaRecuperacionDto> findAll() {
        return notaRecuperacionRepository.findAll().stream()
                .map(NotaRecuperacionDto::fromEntity)
                .collect(Collectors.toList());
    }

    public PageResponse<NotaRecuperacionDto> findWithFilters(
            UUID alumnoId,
            UUID cursoId,
            UUID anioEscolarId,
            Boolean aprobado,
            Pageable pageable) {
        Page<NotaRecuperacionDto> page = notaRecuperacionRepository.findWithFilters(
                alumnoId, cursoId, anioEscolarId, aprobado, pageable)
                .map(NotaRecuperacionDto::fromEntity);
        return PageResponse.from(page);
    }

    public NotaRecuperacionDto findById(UUID id) {
        NotaRecuperacion nota = notaRecuperacionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Nota de recuperación no encontrada: " + id));
        return NotaRecuperacionDto.fromEntity(nota);
    }

    public List<NotaRecuperacionDto> findByAlumno(UUID alumnoId) {
        return notaRecuperacionRepository.findByAlumnoId(alumnoId).stream()
                .map(NotaRecuperacionDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<NotaRecuperacionDto> findByAlumnoAndAnioEscolar(UUID alumnoId, UUID anioEscolarId) {
        return notaRecuperacionRepository.findByAlumnoIdAndAnioEscolarIdWithDetails(alumnoId, anioEscolarId).stream()
                .map(NotaRecuperacionDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotaRecuperacionDto create(NotaRecuperacionCreateRequest request, UUID docenteId) {
        // Validate alumno
        Alumno alumno = alumnoRepository.findById(request.getAlumnoId())
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado: " + request.getAlumnoId()));

        // Validate curso
        Curso curso = cursoRepository.findById(request.getCursoId())
                .orElseThrow(() -> new EntityNotFoundException("Curso no encontrado: " + request.getCursoId()));

        // Validate anio escolar
        AnioEscolar anioEscolar = anioEscolarRepository.findById(request.getAnioEscolarId())
                .orElseThrow(() -> new EntityNotFoundException("Año escolar no encontrado: " + request.getAnioEscolarId()));

        // Check for duplicate
        if (notaRecuperacionRepository.existsByAlumnoIdAndCursoIdAndAnioEscolarId(
                request.getAlumnoId(), request.getCursoId(), request.getAnioEscolarId())) {
            throw new IllegalArgumentException("Ya existe una nota de recuperación para este alumno, curso y año escolar");
        }

        // Get docente if provided
        Docente docente = null;
        if (docenteId != null) {
            docente = docenteRepository.findById(docenteId).orElse(null);
        }

        // Create nota recuperacion
        NotaRecuperacion nota = NotaRecuperacion.builder()
                .alumno(alumno)
                .curso(curso)
                .anioEscolar(anioEscolar)
                .notaOriginal(request.getNotaOriginal())
                .notaRecuperacion(request.getNotaRecuperacion())
                .fechaExamen(request.getFechaExamen())
                .observaciones(request.getObservaciones())
                .docente(docente)
                .build();

        // Calculate if passed (>= 11)
        nota.calcularAprobado();

        NotaRecuperacion saved = notaRecuperacionRepository.save(nota);
        log.info("Nota de recuperación creada para alumno {} en curso {}", 
                alumno.getNombreCompleto(), curso.getNombre());

        return NotaRecuperacionDto.fromEntity(saved);
    }

    @Transactional
    public NotaRecuperacionDto update(UUID id, NotaRecuperacionCreateRequest request) {
        NotaRecuperacion nota = notaRecuperacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nota de recuperación no encontrada: " + id));

        nota.setNotaOriginal(request.getNotaOriginal());
        nota.setNotaRecuperacion(request.getNotaRecuperacion());
        nota.setFechaExamen(request.getFechaExamen());
        nota.setObservaciones(request.getObservaciones());

        // Recalculate if passed
        nota.calcularAprobado();

        NotaRecuperacion saved = notaRecuperacionRepository.save(nota);
        log.info("Nota de recuperación actualizada: {}", id);

        return NotaRecuperacionDto.fromEntity(saved);
    }

    @Transactional
    public void delete(UUID id) {
        NotaRecuperacion nota = notaRecuperacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nota de recuperación no encontrada: " + id));

        notaRecuperacionRepository.delete(nota);
        log.info("Nota de recuperación eliminada: {}", id);
    }
}
