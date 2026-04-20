package pe.edu.colegioricardopalma.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.dto.PensionCreateRequest;
import pe.edu.colegioricardopalma.dto.PensionDto;
import pe.edu.colegioricardopalma.entity.PensionEstado;
import pe.edu.colegioricardopalma.repository.AlumnoApoderadoRepository;
import pe.edu.colegioricardopalma.repository.ApoderadoRepository;
import pe.edu.colegioricardopalma.repository.UsuarioRepository;
import pe.edu.colegioricardopalma.service.PensionService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pensiones")
public class PensionController {

    private final PensionService pensionService;
    private final UsuarioRepository usuarioRepository;
    private final ApoderadoRepository apoderadoRepository;
    private final AlumnoApoderadoRepository alumnoApoderadoRepository;

    public PensionController(
            PensionService pensionService,
            UsuarioRepository usuarioRepository,
            ApoderadoRepository apoderadoRepository,
            AlumnoApoderadoRepository alumnoApoderadoRepository
    ) {
        this.pensionService = pensionService;
        this.usuarioRepository = usuarioRepository;
        this.apoderadoRepository = apoderadoRepository;
        this.alumnoApoderadoRepository = alumnoApoderadoRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PADRE')")
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) PensionEstado estado,
            @RequestParam(required = false) UUID gradoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // If PADRE role, only return their children's pensions
        if (isPadreRole(userDetails)) {
            List<UUID> hijosIds = getHijosIds(userDetails);
            List<PensionDto> pensiones = pensionService.findPendientesByAlumnos(hijosIds);
            return ResponseEntity.ok(pensiones);
        }

        // Admin: return paginated results
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("mes").ascending());
        return ResponseEntity.ok(pensionService.findWithFilters(mes, estado, gradoId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PADRE')")
    public ResponseEntity<PensionDto> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PensionDto pension = pensionService.findById(id);
        
        // Verify PADRE can only see their children's pensions
        if (isPadreRole(userDetails)) {
            List<UUID> hijosIds = getHijosIds(userDetails);
            if (!hijosIds.contains(pension.getAlumnoId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        return ResponseEntity.ok(pension);
    }

    @GetMapping("/alumno/{alumnoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PADRE')")
    public ResponseEntity<List<PensionDto>> findByAlumno(
            @PathVariable UUID alumnoId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Verify PADRE can only see their children's pensions
        if (isPadreRole(userDetails)) {
            List<UUID> hijosIds = getHijosIds(userDetails);
            if (!hijosIds.contains(alumnoId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        return ResponseEntity.ok(pensionService.findByAlumno(alumnoId));
    }

    @GetMapping("/alumno/{alumnoId}/pendientes")
    @PreAuthorize("hasAnyRole('ADMIN', 'PADRE')")
    public ResponseEntity<List<PensionDto>> findPendientesByAlumno(
            @PathVariable UUID alumnoId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Verify PADRE can only see their children's pensions
        if (isPadreRole(userDetails)) {
            List<UUID> hijosIds = getHijosIds(userDetails);
            if (!hijosIds.contains(alumnoId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        return ResponseEntity.ok(pensionService.findPendientesByAlumno(alumnoId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PensionDto> create(@Valid @RequestBody PensionCreateRequest request) {
        PensionDto created = pensionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/generar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> generarPensiones(
            @RequestParam UUID anioEscolarId,
            @RequestParam(required = false) Integer mes
    ) {
        List<PensionDto> generadas = pensionService.generarPensionesMensuales(anioEscolarId, mes);
        return ResponseEntity.ok(Map.of(
                "message", "Pensiones generadas exitosamente",
                "cantidad", generadas.size(),
                "pensiones", generadas
        ));
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PensionDto> updateEstado(
            @PathVariable UUID id,
            @RequestParam PensionEstado estado
    ) {
        return ResponseEntity.ok(pensionService.updateEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        pensionService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Pensión eliminada exitosamente"));
    }

    private boolean isPadreRole(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PADRE"));
    }

    private List<UUID> getHijosIds(UserDetails userDetails) {
        return usuarioRepository.findByUsername(userDetails.getUsername())
                .flatMap(usuario -> apoderadoRepository.findByUsuarioId(usuario.getId()))
                .map(apoderado -> alumnoApoderadoRepository.findByApoderadoId(apoderado.getId()).stream()
                        .map(aa -> aa.getAlumno().getId())
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }
}
