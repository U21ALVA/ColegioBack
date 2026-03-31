package pe.edu.colegioricardopalma.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.colegioricardopalma.dto.AlumnoDto;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.service.AlumnoService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/alumnos")
@RequiredArgsConstructor
public class AlumnoController {

    private final AlumnoService alumnoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AlumnoDto>> findAll() {
        return ResponseEntity.ok(alumnoService.findAll());
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AlumnoDto>> findAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("apellidos", "nombres"));
        return ResponseEntity.ok(alumnoService.findAllPaginated(pageable));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AlumnoDto>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID gradoId,
            @RequestParam(required = false) UUID seccionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("apellidos", "nombres"));
        if (gradoId != null || seccionId != null) {
            return ResponseEntity.ok(alumnoService.searchWithFilters(q, gradoId, seccionId, pageable));
        }
        return ResponseEntity.ok(alumnoService.search(q, pageable));
    }

    @GetMapping("/grado/{gradoId}/seccion/{seccionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<List<AlumnoDto>> findByGradoAndSeccion(
            @PathVariable UUID gradoId,
            @PathVariable UUID seccionId
    ) {
        return ResponseEntity.ok(alumnoService.findByGradoAndSeccion(gradoId, seccionId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlumnoDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(alumnoService.findById(id));
    }

    @GetMapping("/dni/{dni}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlumnoDto> findByDni(@PathVariable String dni) {
        return ResponseEntity.ok(alumnoService.findByDni(dni));
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> countActivos() {
        return ResponseEntity.ok(Map.of("total", alumnoService.countActivos()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlumnoDto> create(@Valid @RequestBody AlumnoDto dto) {
        AlumnoDto created = alumnoService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlumnoDto> update(@PathVariable UUID id, @Valid @RequestBody AlumnoDto dto) {
        return ResponseEntity.ok(alumnoService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        alumnoService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Alumno eliminado exitosamente"));
    }
}
