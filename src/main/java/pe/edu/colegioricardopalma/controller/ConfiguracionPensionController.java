package pe.edu.colegioricardopalma.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.colegioricardopalma.dto.ConfiguracionPensionCreateRequest;
import pe.edu.colegioricardopalma.dto.ConfiguracionPensionDto;
import pe.edu.colegioricardopalma.service.ConfiguracionPensionService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/configuracion-pension")
@RequiredArgsConstructor
public class ConfiguracionPensionController {

    private final ConfiguracionPensionService configuracionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ConfiguracionPensionDto>> findAll() {
        return ResponseEntity.ok(configuracionService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfiguracionPensionDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(configuracionService.findById(id));
    }

    @GetMapping("/activo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfiguracionPensionDto> findByAnioEscolarActivo() {
        ConfiguracionPensionDto config = configuracionService.findByAnioEscolarActivo();
        if (config == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(config);
    }

    @GetMapping("/anio/{anioEscolarId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfiguracionPensionDto> findByAnioEscolar(@PathVariable UUID anioEscolarId) {
        ConfiguracionPensionDto config = configuracionService.findByAnioEscolarId(anioEscolarId);
        if (config == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(config);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfiguracionPensionDto> create(
            @Valid @RequestBody ConfiguracionPensionCreateRequest request) {
        ConfiguracionPensionDto created = configuracionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfiguracionPensionDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody ConfiguracionPensionCreateRequest request) {
        return ResponseEntity.ok(configuracionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        configuracionService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Configuración eliminada exitosamente"));
    }
}
