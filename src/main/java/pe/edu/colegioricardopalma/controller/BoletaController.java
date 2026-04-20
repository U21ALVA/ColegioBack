package pe.edu.colegioricardopalma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.edu.colegioricardopalma.dto.BoletaDto;
import pe.edu.colegioricardopalma.dto.ResumenAcademicoDto;
import pe.edu.colegioricardopalma.entity.Usuario;
import pe.edu.colegioricardopalma.repository.AnioEscolarRepository;
import pe.edu.colegioricardopalma.repository.UsuarioRepository;
import pe.edu.colegioricardopalma.service.BoletaService;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/boletas")
public class BoletaController {

    private final BoletaService boletaService;
    private final AnioEscolarRepository anioEscolarRepository;
    private final UsuarioRepository usuarioRepository;

    public BoletaController(
            BoletaService boletaService,
            AnioEscolarRepository anioEscolarRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.boletaService = boletaService;
        this.anioEscolarRepository = anioEscolarRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/alumno/{alumnoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR', 'PADRE')")
    public ResponseEntity<BoletaDto> getBoletaAlumno(
            @PathVariable UUID alumnoId,
            @RequestParam(required = false) UUID anioEscolarId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // If no year specified, use active year
        if (anioEscolarId == null) {
            anioEscolarId = anioEscolarRepository.findByActivoTrue()
                    .orElseThrow(() -> new EntityNotFoundException("No hay año escolar activo"))
                    .getId();
        }

        // For PADRE role, validate access to this student
        if (isPadre(userDetails)) {
            validatePadreAccess(userDetails, alumnoId);
        }

        return ResponseEntity.ok(boletaService.generateBoleta(alumnoId, anioEscolarId));
    }

    @GetMapping("/alumno/{alumnoId}/bimestre/{bimestreId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR', 'PADRE')")
    public ResponseEntity<BoletaDto> getBoletaAlumnoBimestre(
            @PathVariable UUID alumnoId,
            @PathVariable UUID bimestreId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // For PADRE role, validate access to this student
        if (isPadre(userDetails)) {
            validatePadreAccess(userDetails, alumnoId);
        }

        return ResponseEntity.ok(boletaService.generateBoletaBimestre(alumnoId, bimestreId));
    }

    @GetMapping("/alumno/{alumnoId}/resumen")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR', 'PADRE')")
    public ResponseEntity<ResumenAcademicoDto> getResumenAcademico(
            @PathVariable UUID alumnoId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // For PADRE role, validate access to this student
        if (isPadre(userDetails)) {
            validatePadreAccess(userDetails, alumnoId);
        }

        return ResponseEntity.ok(boletaService.generateResumenAcademico(alumnoId));
    }

    @GetMapping("/mis-hijos")
    @PreAuthorize("hasRole('PADRE')")
    public ResponseEntity<List<ResumenAcademicoDto>> getMisHijosResumen(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID apoderadoId = getApoderadoIdFromUser(userDetails);
        if (apoderadoId == null) {
            throw new EntityNotFoundException("Apoderado no encontrado para el usuario actual");
        }

        List<UUID> hijosIds = boletaService.getHijosForApoderado(apoderadoId);
        
        List<ResumenAcademicoDto> resumenes = hijosIds.stream()
                .map(boletaService::generateResumenAcademico)
                .collect(Collectors.toList());

        return ResponseEntity.ok(resumenes);
    }

    private boolean isPadre(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PADRE"));
    }

    private void validatePadreAccess(UserDetails userDetails, UUID alumnoId) {
        UUID apoderadoId = getApoderadoIdFromUser(userDetails);
        if (apoderadoId == null) {
            throw new SecurityException("Apoderado no encontrado");
        }
        
        if (!boletaService.isApoderadoOfAlumno(apoderadoId, alumnoId)) {
            throw new SecurityException("No tiene permiso para ver las notas de este alumno");
        }
    }

    private UUID getApoderadoIdFromUser(UserDetails userDetails) {
        Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername())
                .orElse(null);
        if (usuario == null) {
            return null;
        }
        return boletaService.getApoderadoIdByUsuarioId(usuario.getId());
    }
}
