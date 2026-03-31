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
import pe.edu.colegioricardopalma.dto.GradoDto;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.entity.Nivel;
import pe.edu.colegioricardopalma.service.GradoService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/grados")
@RequiredArgsConstructor
public class GradoController {

    private final GradoService gradoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GradoDto>> findAll() {
        return ResponseEntity.ok(gradoService.findAll());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<GradoDto>> findAllActivos() {
        return ResponseEntity.ok(gradoService.findAllActivos());
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<GradoDto>> findAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("orden"));
        return ResponseEntity.ok(gradoService.findAllPaginated(pageable));
    }

    @GetMapping("/nivel/{nivel}")
    public ResponseEntity<List<GradoDto>> findByNivel(@PathVariable Nivel nivel) {
        return ResponseEntity.ok(gradoService.findByNivel(nivel));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GradoDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(gradoService.findById(id));
    }

    @GetMapping("/{id}/secciones")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GradoDto> findByIdWithSecciones(@PathVariable UUID id) {
        return ResponseEntity.ok(gradoService.findByIdWithSecciones(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GradoDto> create(@Valid @RequestBody GradoDto dto) {
        GradoDto created = gradoService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GradoDto> update(@PathVariable UUID id, @Valid @RequestBody GradoDto dto) {
        return ResponseEntity.ok(gradoService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        gradoService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Grado eliminado exitosamente"));
    }
}
