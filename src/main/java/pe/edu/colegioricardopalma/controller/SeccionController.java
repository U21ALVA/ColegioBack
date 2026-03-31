package pe.edu.colegioricardopalma.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.colegioricardopalma.dto.SeccionDto;
import pe.edu.colegioricardopalma.service.SeccionService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/secciones")
@RequiredArgsConstructor
public class SeccionController {

    private final SeccionService seccionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SeccionDto>> findAll() {
        return ResponseEntity.ok(seccionService.findAll());
    }

    @GetMapping("/grado/{gradoId}")
    public ResponseEntity<List<SeccionDto>> findByGradoId(@PathVariable UUID gradoId) {
        return ResponseEntity.ok(seccionService.findByGradoId(gradoId));
    }

    @GetMapping("/grado/{gradoId}/activas")
    public ResponseEntity<List<SeccionDto>> findByGradoIdActivas(@PathVariable UUID gradoId) {
        return ResponseEntity.ok(seccionService.findByGradoIdActivas(gradoId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeccionDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(seccionService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeccionDto> create(@Valid @RequestBody SeccionDto dto) {
        SeccionDto created = seccionService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeccionDto> update(@PathVariable UUID id, @Valid @RequestBody SeccionDto dto) {
        return ResponseEntity.ok(seccionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        seccionService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Sección eliminada exitosamente"));
    }
}
