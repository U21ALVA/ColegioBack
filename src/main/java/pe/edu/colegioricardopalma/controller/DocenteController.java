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
import pe.edu.colegioricardopalma.dto.DocenteDto;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.service.DocenteService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/docentes")
@RequiredArgsConstructor
public class DocenteController {

    private final DocenteService docenteService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DocenteDto>> findAll() {
        return ResponseEntity.ok(docenteService.findAll());
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<DocenteDto>> findAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("apellidos", "nombres"));
        return ResponseEntity.ok(docenteService.findAllPaginated(pageable));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<DocenteDto>> search(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("apellidos", "nombres"));
        return ResponseEntity.ok(docenteService.search(q, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocenteDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(docenteService.findById(id));
    }

    @GetMapping("/dni/{dni}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocenteDto> findByDni(@PathVariable String dni) {
        return ResponseEntity.ok(docenteService.findByDni(dni));
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> countActivos() {
        return ResponseEntity.ok(Map.of("total", docenteService.countActivos()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocenteDto> create(@Valid @RequestBody DocenteDto dto) {
        DocenteDto created = docenteService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocenteDto> update(@PathVariable UUID id, @Valid @RequestBody DocenteDto dto) {
        return ResponseEntity.ok(docenteService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        docenteService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Docente eliminado exitosamente"));
    }
}
