package pe.edu.colegioricardopalma.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.colegioricardopalma.dto.AlumnoDto;
import pe.edu.colegioricardopalma.dto.MatriculaBulkRequest;
import pe.edu.colegioricardopalma.dto.MatriculaCreateRequest;
import pe.edu.colegioricardopalma.dto.MatriculaDto;
import pe.edu.colegioricardopalma.service.MatriculaService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/matriculas")
@RequiredArgsConstructor
public class MatriculaController {

    private final MatriculaService matriculaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<List<MatriculaDto>> findAll(
            @RequestParam(required = false) UUID anioEscolarId,
            @RequestParam(required = false) UUID seccionId,
            @RequestParam(required = false) UUID cursoId,
            @RequestParam(required = false) UUID alumnoId
    ) {
        return ResponseEntity.ok(matriculaService.findWithFilters(anioEscolarId, seccionId, cursoId, alumnoId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MatriculaDto> create(@Valid @RequestBody MatriculaCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matriculaService.create(request));
    }

    @PostMapping("/generar-seccion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MatriculaDto>> generar(@Valid @RequestBody MatriculaBulkRequest request) {
        return ResponseEntity.ok(matriculaService.generarPorSeccion(request.getSeccionId(), request.getAnioEscolarId()));
    }

    @GetMapping("/alumnos")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<List<AlumnoDto>> alumnosMatriculados(
            @RequestParam UUID cursoId,
            @RequestParam UUID seccionId,
            @RequestParam UUID anioEscolarId
    ) {
        return ResponseEntity.ok(matriculaService.findAlumnosMatriculados(cursoId, seccionId, anioEscolarId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        matriculaService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Matrícula eliminada"));
    }
}
