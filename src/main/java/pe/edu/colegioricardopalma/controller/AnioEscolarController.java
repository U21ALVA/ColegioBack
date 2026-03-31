package pe.edu.colegioricardopalma.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.colegioricardopalma.dto.AnioEscolarDto;
import pe.edu.colegioricardopalma.service.AnioEscolarService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/anios-escolares")
@RequiredArgsConstructor
public class AnioEscolarController {

    private final AnioEscolarService anioEscolarService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AnioEscolarDto>> findAll() {
        return ResponseEntity.ok(anioEscolarService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnioEscolarDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(anioEscolarService.findById(id));
    }

    @GetMapping("/activo")
    public ResponseEntity<AnioEscolarDto> findActivo() {
        return ResponseEntity.ok(anioEscolarService.findActivo());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnioEscolarDto> create(@Valid @RequestBody AnioEscolarDto dto) {
        AnioEscolarDto created = anioEscolarService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnioEscolarDto> update(@PathVariable UUID id, @Valid @RequestBody AnioEscolarDto dto) {
        return ResponseEntity.ok(anioEscolarService.update(id, dto));
    }

    @PostMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnioEscolarDto> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(anioEscolarService.activate(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        anioEscolarService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Año escolar eliminado exitosamente"));
    }
}
