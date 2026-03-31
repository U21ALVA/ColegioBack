package pe.edu.colegioricardopalma.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.edu.colegioricardopalma.dto.ComunicadoCreateRequest;
import pe.edu.colegioricardopalma.dto.ComunicadoDto;
import pe.edu.colegioricardopalma.dto.ComunicadoEntregaDto;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.service.ComunicadoService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comunicados")
@RequiredArgsConstructor
public class ComunicadoController {

    private final ComunicadoService comunicadoService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<ComunicadoDto>> listAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        return ResponseEntity.ok(comunicadoService.listAdmin(pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComunicadoDto> create(
            @Valid @RequestBody ComunicadoCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ComunicadoDto dto = comunicadoService.createBorrador(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/{id}/publicar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComunicadoDto> publicar(@PathVariable UUID id) {
        return ResponseEntity.ok(comunicadoService.publicar(id));
    }

    @GetMapping("/mis-comunicados")
    @PreAuthorize("hasAnyRole('PADRE', 'ADMIN')")
    public ResponseEntity<PageResponse<ComunicadoDto>> misComunicados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(comunicadoService.listParent(userDetails.getUsername(), page, size));
    }

    @GetMapping("/{id}/entregas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ComunicadoEntregaDto>> entregas(@PathVariable UUID id) {
        return ResponseEntity.ok(comunicadoService.listEntregas(id));
    }
}
