package pe.edu.colegioricardopalma.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.dto.PagoDto;
import pe.edu.colegioricardopalma.entity.PagoEstado;
import pe.edu.colegioricardopalma.service.PagoService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<PagoDto>> findAll(
            @RequestParam(required = false) PagoEstado estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        return ResponseEntity.ok(pagoService.findWithFilters(estado, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagoDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(pagoService.findById(id));
    }

    @GetMapping("/pension/{pensionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PADRE')")
    public ResponseEntity<List<PagoDto>> findByPension(@PathVariable UUID pensionId) {
        return ResponseEntity.ok(pagoService.findByPension(pensionId));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats(
            @RequestParam(required = false) UUID anioEscolarId
    ) {
        BigDecimal totalRecaudado = anioEscolarId != null 
                ? pagoService.getTotalRecaudadoAnioEscolar(anioEscolarId)
                : BigDecimal.ZERO;
        
        Long cantidadPagos = anioEscolarId != null 
                ? pagoService.countCompletadosAnioEscolar(anioEscolarId)
                : 0L;

        return ResponseEntity.ok(Map.of(
                "totalRecaudado", totalRecaudado,
                "cantidadPagos", cantidadPagos
        ));
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagoDto> updateEstado(
            @PathVariable UUID id,
            @RequestParam PagoEstado estado
    ) {
        return ResponseEntity.ok(pagoService.updateEstado(id, estado));
    }
}
