package pe.edu.colegioricardopalma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.*;
import pe.edu.colegioricardopalma.entity.*;
import pe.edu.colegioricardopalma.repository.*;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotaService {

    private final NotaRepository notaRepository;
    private final NotaHistorialRepository notaHistorialRepository;
    private final AlumnoRepository alumnoRepository;
    private final CursoRepository cursoRepository;
    private final BimestreRepository bimestreRepository;
    private final DocenteRepository docenteRepository;
    private final DocenteCursoRepository docenteCursoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlumnoCursoRepository alumnoCursoRepository;

    @Transactional(readOnly = true)
    public List<NotaDto> findAll() {
        return notaRepository.findAll().stream()
                .map(NotaDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<NotaDto> findWithFilters(
            UUID alumnoId,
            UUID cursoId,
            UUID bimestreId,
            UUID docenteId,
            Pageable pageable) {
        Page<NotaDto> page = notaRepository.findWithFilters(alumnoId, cursoId, bimestreId, docenteId, pageable)
                .map(NotaDto::fromEntity);
        return PageResponse.from(page);
    }

    public NotaDto findById(UUID id) {
        Nota nota = notaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Nota no encontrada: " + id));
        return NotaDto.fromEntity(nota);
    }

    public List<NotaDto> findByAlumno(UUID alumnoId) {
        return notaRepository.findByAlumnoIdWithDetails(alumnoId).stream()
                .map(NotaDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<NotaDto> findByAlumnoAndBimestre(UUID alumnoId, UUID bimestreId) {
        return notaRepository.findByAlumnoIdAndBimestreId(alumnoId, bimestreId).stream()
                .map(NotaDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<NotaDto> findByCursoAndBimestre(UUID cursoId, UUID bimestreId) {
        return notaRepository.findByCursoIdAndBimestreId(cursoId, bimestreId).stream()
                .map(NotaDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotaDto create(NotaCreateRequest request, UUID docenteId) {
        // Validate alumno
        Alumno alumno = alumnoRepository.findById(request.getAlumnoId())
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado: " + request.getAlumnoId()));

        // Validate curso
        Curso curso = cursoRepository.findById(request.getCursoId())
                .orElseThrow(() -> new EntityNotFoundException("Curso no encontrado: " + request.getCursoId()));

        // Validate bimestre and check if it's open
        Bimestre bimestre = bimestreRepository.findByIdWithAnioEscolar(request.getBimestreId())
                .orElseThrow(() -> new EntityNotFoundException("Bimestre no encontrado: " + request.getBimestreId()));

        validateBimestreOpen(bimestre);
        validateAlumnoMatriculadoEnCurso(alumno, curso.getId(), bimestre.getAnioEscolar().getId());

        // Check for duplicate
        if (notaRepository.existsByAlumnoIdAndCursoIdAndBimestreId(
                request.getAlumnoId(), request.getCursoId(), request.getBimestreId())) {
            throw new IllegalArgumentException("Ya existe una nota para este alumno, curso y bimestre");
        }

        // Get docente if provided
        Docente docente = null;
        if (docenteId != null) {
            docente = docenteRepository.findById(docenteId).orElse(null);
        }

        // Create nota
        Nota nota = Nota.builder()
                .alumno(alumno)
                .curso(curso)
                .bimestre(bimestre)
                .n1(request.getN1())
                .n2(request.getN2())
                .n3(request.getN3())
                .n4(request.getN4())
                .docente(docente)
                .build();

        // Calculate final grade and literal
        nota.calcularNotaFinal();

        Nota saved = notaRepository.save(nota);
        log.info("Nota creada para alumno {} en curso {} bimestre {}", 
                alumno.getNombreCompleto(), curso.getNombre(), bimestre.getNumero());

        return NotaDto.fromEntity(saved);
    }

    @Transactional
    public NotaDto update(UUID id, NotaCreateRequest request) {
        Nota nota = notaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Nota no encontrada: " + id));

        // Validate bimestre is open
        validateBimestreOpen(nota.getBimestre());

        // Get current user for audit
        Usuario usuario = getCurrentUsuario();

        // Log changes
        logGradeChange(nota, "n1", nota.getN1(), request.getN1(), usuario, request.getJustificacion());
        logGradeChange(nota, "n2", nota.getN2(), request.getN2(), usuario, request.getJustificacion());
        logGradeChange(nota, "n3", nota.getN3(), request.getN3(), usuario, request.getJustificacion());
        logGradeChange(nota, "n4", nota.getN4(), request.getN4(), usuario, request.getJustificacion());

        // Update grades
        nota.setN1(request.getN1());
        nota.setN2(request.getN2());
        nota.setN3(request.getN3());
        nota.setN4(request.getN4());

        // Recalculate final grade
        nota.calcularNotaFinal();

        Nota saved = notaRepository.save(nota);
        log.info("Nota actualizada: {} - {} - Bim {}", 
                nota.getAlumno().getNombreCompleto(), 
                nota.getCurso().getNombre(), 
                nota.getBimestre().getNumero());

        return NotaDto.fromEntity(saved);
    }

    @Transactional
    public List<NotaDto> bulkCreateOrUpdate(NotaBulkRequest request, UUID docenteId) {
        // Validate curso
        Curso curso = cursoRepository.findById(request.getCursoId())
                .orElseThrow(() -> new EntityNotFoundException("Curso no encontrado: " + request.getCursoId()));

        // Validate bimestre and check if it's open
        Bimestre bimestre = bimestreRepository.findByIdWithAnioEscolar(request.getBimestreId())
                .orElseThrow(() -> new EntityNotFoundException("Bimestre no encontrado: " + request.getBimestreId()));

        validateBimestreOpen(bimestre);

        // Get docente
        Docente docente = null;
        if (docenteId != null) {
            docente = docenteRepository.findById(docenteId).orElse(null);
        }

        // Get current user for audit
        Usuario usuario = getCurrentUsuario();

        List<NotaDto> results = new ArrayList<>();

        for (NotaBulkRequest.NotaAlumnoRequest notaRequest : request.getNotas()) {
            // Validate alumno
            Alumno alumno = alumnoRepository.findById(notaRequest.getAlumnoId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Alumno no encontrado: " + notaRequest.getAlumnoId()));

            validateAlumnoMatriculadoEnCurso(alumno, request.getCursoId(), bimestre.getAnioEscolar().getId());

            // Check if nota already exists
            Nota nota = notaRepository.findByAlumnoIdAndCursoIdAndBimestreId(
                    notaRequest.getAlumnoId(), request.getCursoId(), request.getBimestreId())
                    .orElse(null);

            if (nota != null) {
                // Update existing
                logGradeChange(nota, "n1", nota.getN1(), notaRequest.getN1(), usuario, request.getJustificacion());
                logGradeChange(nota, "n2", nota.getN2(), notaRequest.getN2(), usuario, request.getJustificacion());
                logGradeChange(nota, "n3", nota.getN3(), notaRequest.getN3(), usuario, request.getJustificacion());
                logGradeChange(nota, "n4", nota.getN4(), notaRequest.getN4(), usuario, request.getJustificacion());

                nota.setN1(notaRequest.getN1());
                nota.setN2(notaRequest.getN2());
                nota.setN3(notaRequest.getN3());
                nota.setN4(notaRequest.getN4());
            } else {
                // Create new
                nota = Nota.builder()
                        .alumno(alumno)
                        .curso(curso)
                        .bimestre(bimestre)
                        .n1(notaRequest.getN1())
                        .n2(notaRequest.getN2())
                        .n3(notaRequest.getN3())
                        .n4(notaRequest.getN4())
                        .docente(docente)
                        .build();
            }

            nota.calcularNotaFinal();
            Nota saved = notaRepository.save(nota);
            results.add(NotaDto.fromEntity(saved));
        }

        log.info("Bulk upload: {} notas procesadas para curso {} bimestre {}", 
                results.size(), curso.getNombre(), bimestre.getNumero());

        return results;
    }

    @Transactional
    public void delete(UUID id) {
        Nota nota = notaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nota no encontrada: " + id));

        // Validate bimestre is open
        validateBimestreOpen(nota.getBimestre());

        notaRepository.delete(nota);
        log.info("Nota eliminada: {}", id);
    }

    /**
     * Check if teacher is assigned to the course for the active school year.
     */
    public boolean isDocenteAssignedToCurso(UUID docenteId, UUID cursoId, UUID gradoId, UUID seccionId) {
        // Get active school year
        // For now, we'll check if there's any active assignment
        List<DocenteCurso> assignments = docenteCursoRepository.findByDocenteIdAndAnioEscolarId(
                docenteId, null);
        
        return assignments.stream().anyMatch(dc -> 
                dc.getCurso().getId().equals(cursoId) &&
                dc.getGrado().getId().equals(gradoId) &&
                dc.getSeccion().getId().equals(seccionId) &&
                dc.getEstado() == Estado.ACTIVO);
    }

    private void validateBimestreOpen(Bimestre bimestre) {
        if (bimestre.getCerrado()) {
            throw new IllegalStateException(
                    "El bimestre " + bimestre.getNumero() + " está cerrado. No se pueden modificar notas.");
        }
    }

    private void validateAlumnoMatriculadoEnCurso(Alumno alumno, UUID cursoId, UUID anioEscolarId) {
        if (alumno.getSeccion() == null) {
            throw new IllegalArgumentException("El alumno " + alumno.getNombreCompleto() + " no tiene sección asignada");
        }

        boolean matriculado = alumnoCursoRepository
                .existsByAlumnoIdAndCursoIdAndSeccionIdAndAnioEscolarIdAndEstado(
                        alumno.getId(),
                        cursoId,
                        alumno.getSeccion().getId(),
                        anioEscolarId,
                        Estado.ACTIVO
                );

        if (!matriculado) {
            throw new IllegalArgumentException("El alumno " + alumno.getNombreCompleto() + " no está matriculado en el curso seleccionado");
        }
    }

    private void logGradeChange(Nota nota, String campo, BigDecimal valorAnterior, BigDecimal valorNuevo, Usuario usuario, String justificacion) {
        if (valorAnterior == null && valorNuevo == null) {
            return;
        }
        if (valorAnterior != null && valorAnterior.equals(valorNuevo)) {
            return;
        }
        if (justificacion == null || justificacion.trim().isEmpty()) {
            throw new IllegalArgumentException("La justificacion es obligatoria para modificar notas");
        }

        NotaHistorial historial = NotaHistorial.builder()
                .nota(nota)
                .campoModificado(campo)
                .valorAnterior(valorAnterior != null ? valorAnterior.toString() : null)
                .valorNuevo(valorNuevo != null ? valorNuevo.toString() : null)
                .motivo(justificacion.trim())
                .usuario(usuario)
                .build();

        notaHistorialRepository.save(historial);
    }

    private Usuario getCurrentUsuario() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            return usuarioRepository.findByUsername(username).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

}
