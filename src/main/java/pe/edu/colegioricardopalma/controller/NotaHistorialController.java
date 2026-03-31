package pe.edu.colegioricardopalma.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.colegioricardopalma.dto.NotaHistorialDto;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.repository.NotaHistorialRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notas/historial")
@RequiredArgsConstructor
public class NotaHistorialController {

    private final NotaHistorialRepository notaHistorialRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<NotaHistorialDto>> findAll(
            @RequestParam(required = false) UUID notaId,
            @RequestParam(required = false) UUID usuarioId,
            @RequestParam(required = false) UUID cursoId,
            @RequestParam(required = false) UUID docenteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        
        LocalDateTime desdeDateTime = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaDateTime = hasta != null ? hasta.atTime(LocalTime.MAX) : null;

        PageResponse<NotaHistorialDto> response = PageResponse.from(
                notaHistorialRepository.findWithFilters(
                        notaId, usuarioId, cursoId, docenteId, desdeDateTime, hastaDateTime, pageable
                ).map(NotaHistorialDto::fromEntity)
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/nota/{notaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<List<NotaHistorialDto>> findByNota(@PathVariable UUID notaId) {
        List<NotaHistorialDto> historial = notaHistorialRepository.findByNotaIdWithDetails(notaId).stream()
                .map(NotaHistorialDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/curso/{cursoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<List<NotaHistorialDto>> findByCurso(@PathVariable UUID cursoId) {
        List<NotaHistorialDto> historial = notaHistorialRepository.findByCursoIdOrderByCreatedAtDesc(cursoId).stream()
                .map(NotaHistorialDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/rango")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotaHistorialDto>> findByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        LocalDateTime desdeDateTime = desde.atStartOfDay();
        LocalDateTime hastaDateTime = hasta.atTime(LocalTime.MAX);

        List<NotaHistorialDto> historial = notaHistorialRepository.findByDateRange(desdeDateTime, hastaDateTime).stream()
                .map(NotaHistorialDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(historial);
    }
}
