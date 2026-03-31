package pe.edu.colegioricardopalma.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.colegioricardopalma.dto.DocenteCursoDto;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.service.DocenteCursoService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/asignaciones")
@RequiredArgsConstructor
public class DocenteCursoController {

    private final DocenteCursoService docenteCursoService;

    @GetMapping("/anio-escolar/{anioEscolarId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DocenteCursoDto>> findByAnioEscolar(@PathVariable UUID anioEscolarId) {
        return ResponseEntity.ok(docenteCursoService.findByAnioEscolar(anioEscolarId));
    }

    @GetMapping("/anio-escolar/{anioEscolarId}/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<DocenteCursoDto>> findAllPaginated(
            @PathVariable UUID anioEscolarId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ResponseEntity.ok(docenteCursoService.findAllPaginated(anioEscolarId, pageable));
    }

    @GetMapping("/docente/{docenteId}/anio-escolar/{anioEscolarId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<List<DocenteCursoDto>> findByDocenteAndAnioEscolar(
            @PathVariable UUID docenteId,
            @PathVariable UUID anioEscolarId
    ) {
        return ResponseEntity.ok(docenteCursoService.findByDocenteAndAnioEscolar(docenteId, anioEscolarId));
    }

    @GetMapping("/grado/{gradoId}/seccion/{seccionId}/anio-escolar/{anioEscolarId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DocenteCursoDto>> findByGradoSeccionAndAnioEscolar(
            @PathVariable UUID gradoId,
            @PathVariable UUID seccionId,
            @PathVariable UUID anioEscolarId
    ) {
        return ResponseEntity.ok(docenteCursoService.findByGradoSeccionAndAnioEscolar(gradoId, seccionId, anioEscolarId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocenteCursoDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(docenteCursoService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocenteCursoDto> create(@Valid @RequestBody DocenteCursoDto dto) {
        DocenteCursoDto created = docenteCursoService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocenteCursoDto> update(@PathVariable UUID id, @Valid @RequestBody DocenteCursoDto dto) {
        return ResponseEntity.ok(docenteCursoService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        docenteCursoService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Asignación eliminada exitosamente"));
    }
}
