package pe.edu.colegioricardopalma.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.edu.colegioricardopalma.dto.*;
import pe.edu.colegioricardopalma.service.SiagieConfigService;
import pe.edu.colegioricardopalma.service.SiagieExportService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/siagie")
@RequiredArgsConstructor
public class SiagieController {

    private final SiagieConfigService siagieConfigService;
    private final SiagieExportService siagieExportService;

    @GetMapping("/configuracion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfiguracionSiagieDto> getConfiguracion() {
        ConfiguracionSiagieDto dto = siagieConfigService.getLatestConfig();
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/configuracion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfiguracionSiagieDto> upsertConfiguracion(
            @Valid @RequestBody ConfiguracionSiagieDto request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(siagieConfigService.upsert(request, userDetails.getUsername()));
    }

    @PostMapping("/exportar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SiagieExportResponse> exportar(
            @Valid @RequestBody SiagieExportRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(siagieExportService.exportar(request, userDetails.getUsername()));
    }

    @GetMapping("/exportaciones")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<ExportacionSiagieDto>> listExportaciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "fecha", "createdAt"));
        return ResponseEntity.ok(siagieExportService.listExportaciones(pageable));
    }

    @GetMapping("/exportaciones/{id}/download")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        SiagieExportService.ExportFileData data = siagieExportService.getExportFile(id);
        PathResource resource = new PathResource(data.getFilePath());
        String encoded = URLEncoder.encode(data.getFileName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + data.getFileName() + "\"; filename*=UTF-8''" + encoded)
                .body(resource);
    }

    @GetMapping("/cursos-disponibles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CursoDto>> cursosDisponibles() {
        return ResponseEntity.ok(siagieExportService.cursosDisponibles());
    }

    @GetMapping("/filtros")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> filtros() {
        return ResponseEntity.ok(siagieExportService.filtros());
    }
}
