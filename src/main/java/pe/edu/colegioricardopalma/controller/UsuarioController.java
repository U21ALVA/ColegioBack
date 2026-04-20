package pe.edu.colegioricardopalma.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.dto.ResetPasswordRequest;
import pe.edu.colegioricardopalma.dto.UsuarioGestionDto;
import pe.edu.colegioricardopalma.dto.UsuarioGestionRequest;
import pe.edu.colegioricardopalma.service.UsuarioGestionService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioGestionService usuarioGestionService;

    public UsuarioController(UsuarioGestionService usuarioGestionService) {
        this.usuarioGestionService = usuarioGestionService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UsuarioGestionDto>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) UUID gradoId,
            @RequestParam(required = false) UUID seccionId
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("id").ascending());
        return ResponseEntity.ok(usuarioGestionService.findAll(q, tipo, estado, gradoId, seccionId, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioGestionDto> create(@RequestBody UsuarioGestionRequest request) {
        UsuarioGestionDto created = usuarioGestionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioGestionDto> update(@PathVariable UUID id, @RequestBody UsuarioGestionRequest request) {
        return ResponseEntity.ok(usuarioGestionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id, @RequestParam String tipo) {
        usuarioGestionService.delete(id, tipo);
        return ResponseEntity.ok(Map.of("message", "Registro eliminado exitosamente"));
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resetPassword(
            @PathVariable UUID id,
            @RequestParam String tipo,
            @RequestBody ResetPasswordRequest request
    ) {
        usuarioGestionService.resetPassword(id, tipo, request);
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente"));
    }
}
