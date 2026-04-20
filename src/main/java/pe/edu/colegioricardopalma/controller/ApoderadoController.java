package pe.edu.colegioricardopalma.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.colegioricardopalma.dto.AlumnoApoderadoDto;
import pe.edu.colegioricardopalma.dto.ApoderadoDto;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.service.ApoderadoService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/apoderados")
public class ApoderadoController {

    private final ApoderadoService apoderadoService;

    public ApoderadoController(ApoderadoService apoderadoService) {
        this.apoderadoService = apoderadoService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ApoderadoDto>> findAll() {
        return ResponseEntity.ok(apoderadoService.findAll());
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<ApoderadoDto>> findAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("apellidos", "nombres"));
        return ResponseEntity.ok(apoderadoService.findAllPaginated(pageable));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<ApoderadoDto>> search(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("apellidos", "nombres"));
        return ResponseEntity.ok(apoderadoService.search(q, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApoderadoDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(apoderadoService.findById(id));
    }

    @GetMapping("/dni/{dni}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApoderadoDto> findByDni(@PathVariable String dni) {
        return ResponseEntity.ok(apoderadoService.findByDni(dni));
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> countActivos() {
        return ResponseEntity.ok(Map.of("total", apoderadoService.countActivos()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApoderadoDto> create(@Valid @RequestBody ApoderadoDto dto) {
        ApoderadoDto created = apoderadoService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApoderadoDto> update(@PathVariable UUID id, @Valid @RequestBody ApoderadoDto dto) {
        return ResponseEntity.ok(apoderadoService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        apoderadoService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Apoderado eliminado exitosamente"));
    }

    @PostMapping("/{id}/alumnos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlumnoApoderadoDto> linkAlumno(
            @PathVariable UUID id,
            @Valid @RequestBody AlumnoApoderadoDto dto
    ) {
        AlumnoApoderadoDto created = apoderadoService.linkAlumno(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}/alumnos/{alumnoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> unlinkAlumno(
            @PathVariable UUID id,
            @PathVariable UUID alumnoId
    ) {
        apoderadoService.unlinkAlumno(id, alumnoId);
        return ResponseEntity.ok(Map.of("message", "Alumno desvinculado exitosamente"));
    }
}
