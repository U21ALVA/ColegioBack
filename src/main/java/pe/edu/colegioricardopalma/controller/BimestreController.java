package pe.edu.colegioricardopalma.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.colegioricardopalma.dto.BimestreDto;
import pe.edu.colegioricardopalma.service.BimestreService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/bimestres")
@RequiredArgsConstructor
public class BimestreController {

    private final BimestreService bimestreService;

    @GetMapping("/anio-escolar/{anioEscolarId}")
    public ResponseEntity<List<BimestreDto>> findByAnioEscolar(@PathVariable UUID anioEscolarId) {
        return ResponseEntity.ok(bimestreService.findByAnioEscolar(anioEscolarId));
    }

    @GetMapping("/activo")
    public ResponseEntity<List<BimestreDto>> findByAnioActivo() {
        return ResponseEntity.ok(bimestreService.findByAnioActivo());
    }

    @GetMapping("/abiertos")
    public ResponseEntity<List<BimestreDto>> findAbiertosAnioActivo() {
        return ResponseEntity.ok(bimestreService.findAbiertosAnioActivo());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BimestreDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(bimestreService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BimestreDto> create(@Valid @RequestBody BimestreDto dto) {
        BimestreDto created = bimestreService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BimestreDto> update(@PathVariable UUID id, @Valid @RequestBody BimestreDto dto) {
        return ResponseEntity.ok(bimestreService.update(id, dto));
    }

    @PostMapping("/{id}/cerrar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BimestreDto> cerrar(@PathVariable UUID id) {
        return ResponseEntity.ok(bimestreService.cerrar(id));
    }

    @PostMapping("/{id}/reabrir")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BimestreDto> reabrir(@PathVariable UUID id) {
        return ResponseEntity.ok(bimestreService.reabrir(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        bimestreService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Bimestre eliminado exitosamente"));
    }
}
