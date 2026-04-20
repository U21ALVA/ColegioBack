package pe.edu.colegioricardopalma.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.edu.colegioricardopalma.dto.DocenteCursoDto;
import pe.edu.colegioricardopalma.entity.AnioEscolar;
import pe.edu.colegioricardopalma.entity.Docente;
import pe.edu.colegioricardopalma.entity.Usuario;
import pe.edu.colegioricardopalma.repository.AnioEscolarRepository;
import pe.edu.colegioricardopalma.repository.DocenteCursoRepository;
import pe.edu.colegioricardopalma.repository.DocenteRepository;
import pe.edu.colegioricardopalma.repository.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/profesor/cursos")
@RequiredArgsConstructor
public class ProfesorCursoController {

    private final DocenteCursoRepository docenteCursoRepository;
    private final DocenteRepository docenteRepository;
    private final UsuarioRepository usuarioRepository;
    private final AnioEscolarRepository anioEscolarRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<List<DocenteCursoDto>> getMisCursos(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID docenteId = getDocenteIdFromUser(userDetails);
        if (docenteId == null) {
            throw new EntityNotFoundException("Docente no encontrado para el usuario actual");
        }

        // Get active school year
        AnioEscolar anioEscolar = anioEscolarRepository.findByActivoTrue()
                .orElseThrow(() -> new EntityNotFoundException("No hay año escolar activo"));

        List<DocenteCursoDto> cursos = docenteCursoRepository
                .findByDocenteAndAnioEscolarWithDetailsAllStates(docenteId, anioEscolar.getId())
                .stream()
                .filter(dc -> dc.getEstado() == pe.edu.colegioricardopalma.entity.Estado.ACTIVO)
                .map(DocenteCursoDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(cursos);
    }

    @GetMapping("/anio/{anioEscolarId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<List<DocenteCursoDto>> getMisCursosByAnio(
            @PathVariable UUID anioEscolarId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID docenteId = getDocenteIdFromUser(userDetails);
        if (docenteId == null) {
            throw new EntityNotFoundException("Docente no encontrado para el usuario actual");
        }

        List<DocenteCursoDto> cursos = docenteCursoRepository
                .findByDocenteAndAnioEscolarWithDetailsAllStates(docenteId, anioEscolarId)
                .stream()
                .filter(dc -> dc.getEstado() == pe.edu.colegioricardopalma.entity.Estado.ACTIVO)
                .map(DocenteCursoDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(cursos);
    }

    @GetMapping("/docente/{docenteId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DocenteCursoDto>> getCursosByDocente(
            @PathVariable UUID docenteId,
            @RequestParam(required = false) UUID anioEscolarId
    ) {
        // If no year specified, use active year
        if (anioEscolarId == null) {
            anioEscolarId = anioEscolarRepository.findByActivoTrue()
                    .map(AnioEscolar::getId)
                    .orElseThrow(() -> new EntityNotFoundException("No hay año escolar activo"));
        }

        List<DocenteCursoDto> cursos = docenteCursoRepository
                .findByDocenteAndAnioEscolarWithDetails(docenteId, anioEscolarId)
                .stream()
                .map(DocenteCursoDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(cursos);
    }

    private UUID getDocenteIdFromUser(UserDetails userDetails) {
        Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername())
                .orElse(null);
        if (usuario == null) {
            return null;
        }
        
        Docente docente = docenteRepository.findByUsuarioId(usuario.getId())
                .orElse(null);
        
        return docente != null ? docente.getId() : null;
    }
}
