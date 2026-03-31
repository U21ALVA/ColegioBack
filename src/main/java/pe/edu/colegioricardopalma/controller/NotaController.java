package pe.edu.colegioricardopalma.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.edu.colegioricardopalma.dto.*;
import pe.edu.colegioricardopalma.repository.DocenteRepository;
import pe.edu.colegioricardopalma.service.NotaService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notas")
@RequiredArgsConstructor
public class NotaController {

    private final NotaService notaService;
    private final DocenteRepository docenteRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<PageResponse<NotaDto>> findAll(
            @RequestParam(required = false) UUID alumnoId,
            @RequestParam(required = false) UUID cursoId,
            @RequestParam(required = false) UUID bimestreId,
            @RequestParam(required = false) UUID docenteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        return ResponseEntity.ok(notaService.findWithFilters(alumnoId, cursoId, bimestreId, docenteId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<NotaDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(notaService.findById(id));
    }

    @GetMapping("/alumno/{alumnoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR', 'PADRE')")
    public ResponseEntity<List<NotaDto>> findByAlumno(@PathVariable UUID alumnoId) {
        return ResponseEntity.ok(notaService.findByAlumno(alumnoId));
    }

    @GetMapping("/alumno/{alumnoId}/bimestre/{bimestreId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR', 'PADRE')")
    public ResponseEntity<List<NotaDto>> findByAlumnoAndBimestre(
            @PathVariable UUID alumnoId,
            @PathVariable UUID bimestreId
    ) {
        return ResponseEntity.ok(notaService.findByAlumnoAndBimestre(alumnoId, bimestreId));
    }

    @GetMapping("/curso/{cursoId}/bimestre/{bimestreId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<List<NotaDto>> findByCursoAndBimestre(
            @PathVariable UUID cursoId,
            @PathVariable UUID bimestreId
    ) {
        return ResponseEntity.ok(notaService.findByCursoAndBimestre(cursoId, bimestreId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<NotaDto> create(
            @Valid @RequestBody NotaCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID docenteId = getDocenteIdFromUser(userDetails);
        NotaDto created = notaService.create(request, docenteId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<NotaDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody NotaCreateRequest request
    ) {
        return ResponseEntity.ok(notaService.update(id, request));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<List<NotaDto>> bulkCreateOrUpdate(
            @Valid @RequestBody NotaBulkRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID docenteId = getDocenteIdFromUser(userDetails);
        List<NotaDto> results = notaService.bulkCreateOrUpdate(request, docenteId);
        return ResponseEntity.ok(results);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        notaService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Nota eliminada exitosamente"));
    }

    private UUID getDocenteIdFromUser(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return docenteRepository.findByUsuarioId(
                // We need to get the usuario ID from the user details
                // For now, we'll try to find by username
                null
        ).map(d -> d.getId()).orElse(null);
    }
}
