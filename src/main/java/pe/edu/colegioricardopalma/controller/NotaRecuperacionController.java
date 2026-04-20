package pe.edu.colegioricardopalma.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.edu.colegioricardopalma.dto.NotaRecuperacionCreateRequest;
import pe.edu.colegioricardopalma.dto.NotaRecuperacionDto;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.repository.DocenteRepository;
import pe.edu.colegioricardopalma.repository.UsuarioRepository;
import pe.edu.colegioricardopalma.service.NotaRecuperacionService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/recuperaciones")
public class NotaRecuperacionController {

    private final NotaRecuperacionService notaRecuperacionService;
    private final DocenteRepository docenteRepository;
    private final UsuarioRepository usuarioRepository;

    public NotaRecuperacionController(
            NotaRecuperacionService notaRecuperacionService,
            DocenteRepository docenteRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.notaRecuperacionService = notaRecuperacionService;
        this.docenteRepository = docenteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<PageResponse<NotaRecuperacionDto>> findAll(
            @RequestParam(required = false) UUID alumnoId,
            @RequestParam(required = false) UUID cursoId,
            @RequestParam(required = false) UUID anioEscolarId,
            @RequestParam(required = false) Boolean aprobado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        return ResponseEntity.ok(notaRecuperacionService.findWithFilters(
                alumnoId, cursoId, anioEscolarId, aprobado, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<NotaRecuperacionDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(notaRecuperacionService.findById(id));
    }

    @GetMapping("/alumno/{alumnoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR', 'PADRE')")
    public ResponseEntity<List<NotaRecuperacionDto>> findByAlumno(@PathVariable UUID alumnoId) {
        return ResponseEntity.ok(notaRecuperacionService.findByAlumno(alumnoId));
    }

    @GetMapping("/alumno/{alumnoId}/anio/{anioEscolarId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR', 'PADRE')")
    public ResponseEntity<List<NotaRecuperacionDto>> findByAlumnoAndAnioEscolar(
            @PathVariable UUID alumnoId,
            @PathVariable UUID anioEscolarId
    ) {
        return ResponseEntity.ok(notaRecuperacionService.findByAlumnoAndAnioEscolar(alumnoId, anioEscolarId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<NotaRecuperacionDto> create(
            @Valid @RequestBody NotaRecuperacionCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID docenteId = getDocenteIdFromUser(userDetails);
        NotaRecuperacionDto created = notaRecuperacionService.create(request, docenteId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<NotaRecuperacionDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody NotaRecuperacionCreateRequest request
    ) {
        return ResponseEntity.ok(notaRecuperacionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        notaRecuperacionService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Nota de recuperación eliminada exitosamente"));
    }

    private UUID getDocenteIdFromUser(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        var usuarioOpt = usuarioRepository.findByUsername(userDetails.getUsername());
        if (usuarioOpt.isEmpty()) {
            return null;
        }
        var docenteOpt = docenteRepository.findByUsuarioId(usuarioOpt.get().getId());
        if (docenteOpt.isEmpty()) {
            return null;
        }
        return docenteOpt.get().getId();
    }
}
