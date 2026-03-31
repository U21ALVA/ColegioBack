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
import pe.edu.colegioricardopalma.dto.BecaCreateRequest;
import pe.edu.colegioricardopalma.dto.BecaDto;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.service.BecaService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/becas")
@RequiredArgsConstructor
public class BecaController {

    private final BecaService becaService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<BecaDto>> findAll(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Boolean vigente,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        return ResponseEntity.ok(becaService.findWithFilters(tipo, vigente, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BecaDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(becaService.findById(id));
    }

    @GetMapping("/alumno/{alumnoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BecaDto>> findByAlumno(@PathVariable UUID alumnoId) {
        return ResponseEntity.ok(becaService.findByAlumno(alumnoId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BecaDto> create(@Valid @RequestBody BecaCreateRequest request) {
        BecaDto created = becaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BecaDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody BecaCreateRequest request
    ) {
        return ResponseEntity.ok(becaService.update(id, request));
    }

    @PutMapping("/{id}/toggle-vigencia")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BecaDto> toggleVigencia(@PathVariable UUID id) {
        return ResponseEntity.ok(becaService.toggleVigencia(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        becaService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Beca eliminada exitosamente"));
    }
}
