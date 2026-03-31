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
import pe.edu.colegioricardopalma.dto.CursoDto;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.entity.Nivel;
import pe.edu.colegioricardopalma.service.CursoService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cursos")
@RequiredArgsConstructor
public class CursoController {

    private final CursoService cursoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CursoDto>> findAll() {
        return ResponseEntity.ok(cursoService.findAll());
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<CursoDto>> findAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("nivel", "nombre"));
        return ResponseEntity.ok(cursoService.findAllPaginated(pageable));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<CursoDto>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Nivel nivel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("nivel", "nombre"));
        return ResponseEntity.ok(cursoService.search(q, nivel, pageable));
    }

    @GetMapping("/nivel/{nivel}")
    public ResponseEntity<List<CursoDto>> findByNivel(@PathVariable Nivel nivel) {
        return ResponseEntity.ok(cursoService.findByNivel(nivel));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CursoDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(cursoService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CursoDto> create(@Valid @RequestBody CursoDto dto) {
        CursoDto created = cursoService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CursoDto> update(@PathVariable UUID id, @Valid @RequestBody CursoDto dto) {
        return ResponseEntity.ok(cursoService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        cursoService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Curso eliminado exitosamente"));
    }
}
