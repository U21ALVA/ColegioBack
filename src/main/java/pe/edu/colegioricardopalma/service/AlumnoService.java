package pe.edu.colegioricardopalma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.AlumnoApoderadoDto;
import pe.edu.colegioricardopalma.dto.AlumnoDto;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.entity.*;
import pe.edu.colegioricardopalma.repository.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlumnoService {

    private final AlumnoRepository alumnoRepository;
    private final GradoRepository gradoRepository;
    private final SeccionRepository seccionRepository;
    private final AlumnoApoderadoRepository alumnoApoderadoRepository;

    public List<AlumnoDto> findAll() {
        return alumnoRepository.findByEstado(Estado.ACTIVO, Pageable.unpaged())
                .stream()
                .map(AlumnoDto::fromEntity)
                .collect(Collectors.toList());
    }

    public PageResponse<AlumnoDto> findAllPaginated(Pageable pageable) {
        Page<AlumnoDto> page = alumnoRepository.findByEstado(Estado.ACTIVO, pageable)
                .map(AlumnoDto::fromEntity);
        return PageResponse.from(page);
    }

    public PageResponse<AlumnoDto> search(String search, Pageable pageable) {
        Page<AlumnoDto> page = alumnoRepository.searchAlumnos(
                search != null ? search : "",
                Estado.ACTIVO,
                pageable
        ).map(AlumnoDto::fromEntity);
        return PageResponse.from(page);
    }

    public PageResponse<AlumnoDto> searchWithFilters(String search, UUID gradoId, UUID seccionId, Pageable pageable) {
        Page<AlumnoDto> page = alumnoRepository.searchAlumnosWithFilters(
                search != null ? search : "",
                gradoId,
                seccionId,
                Estado.ACTIVO,
                pageable
        ).map(AlumnoDto::fromEntity);
        return PageResponse.from(page);
    }

    public List<AlumnoDto> findByGradoAndSeccion(UUID gradoId, UUID seccionId) {
        return alumnoRepository.findByGradoAndSeccionAndEstado(gradoId, seccionId, Estado.ACTIVO)
                .stream()
                .map(AlumnoDto::fromEntity)
                .collect(Collectors.toList());
    }

    public AlumnoDto findById(UUID id) {
        Alumno alumno = alumnoRepository.findByIdWithGradoAndSeccion(id)
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado: " + id));
        
        AlumnoDto dto = AlumnoDto.fromEntity(alumno);
        
        // Load apoderados
        List<AlumnoApoderadoDto> apoderados = alumnoApoderadoRepository.findByAlumnoIdWithApoderado(id)
                .stream()
                .map(AlumnoApoderadoDto::fromEntity)
                .collect(Collectors.toList());
        dto.setApoderados(apoderados);
        
        return dto;
    }

    public AlumnoDto findByDni(String dni) {
        Alumno alumno = alumnoRepository.findByDni(dni)
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado con DNI: " + dni));
        return AlumnoDto.fromEntity(alumno);
    }

    public Long countActivos() {
        return alumnoRepository.countActivos();
    }

    @Transactional
    public AlumnoDto create(AlumnoDto dto) {
        if (alumnoRepository.existsByDni(dto.getDni())) {
            throw new IllegalArgumentException("Ya existe un alumno con el DNI: " + dto.getDni());
        }

        Alumno alumno = dto.toEntity();

        // Set grado
        if (dto.getGradoId() != null) {
            Grado grado = gradoRepository.findById(dto.getGradoId())
                    .orElseThrow(() -> new EntityNotFoundException("Grado no encontrado: " + dto.getGradoId()));
            alumno.setGrado(grado);
        }

        // Set seccion
        if (dto.getSeccionId() != null) {
            Seccion seccion = seccionRepository.findById(dto.getSeccionId())
                    .orElseThrow(() -> new EntityNotFoundException("Sección no encontrada: " + dto.getSeccionId()));
            alumno.setSeccion(seccion);
        }

        // Generate student code if not provided
        if (alumno.getCodigoEstudiante() == null || alumno.getCodigoEstudiante().isBlank()) {
            alumno.setCodigoEstudiante(generateCodigoEstudiante());
        }

        Alumno saved = alumnoRepository.save(alumno);
        log.info("Alumno creado: {} {}", saved.getNombres(), saved.getApellidos());
        
        return AlumnoDto.fromEntity(saved);
    }

    @Transactional
    public AlumnoDto update(UUID id, AlumnoDto dto) {
        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado: " + id));

        // Check for duplicate DNI
        if (!alumno.getDni().equals(dto.getDni()) && alumnoRepository.existsByDni(dto.getDni())) {
            throw new IllegalArgumentException("Ya existe un alumno con el DNI: " + dto.getDni());
        }

        alumno.setDni(dto.getDni());
        alumno.setNombres(dto.getNombres());
        alumno.setApellidos(dto.getApellidos());
        alumno.setFechaNacimiento(dto.getFechaNacimiento());
        if (dto.getCodigoEstudiante() != null) {
            alumno.setCodigoEstudiante(dto.getCodigoEstudiante());
        }
        if (dto.getEstado() != null) {
            alumno.setEstado(dto.getEstado());
        }

        // Update grado
        if (dto.getGradoId() != null) {
            Grado grado = gradoRepository.findById(dto.getGradoId())
                    .orElseThrow(() -> new EntityNotFoundException("Grado no encontrado: " + dto.getGradoId()));
            alumno.setGrado(grado);
        }

        // Update seccion
        if (dto.getSeccionId() != null) {
            Seccion seccion = seccionRepository.findById(dto.getSeccionId())
                    .orElseThrow(() -> new EntityNotFoundException("Sección no encontrada: " + dto.getSeccionId()));
            alumno.setSeccion(seccion);
        }
        
        Alumno saved = alumnoRepository.save(alumno);
        log.info("Alumno actualizado: {} {}", saved.getNombres(), saved.getApellidos());
        
        return AlumnoDto.fromEntity(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado: " + id));

        alumno.setEstado(Estado.ELIMINADO);
        alumnoRepository.save(alumno);
        log.info("Alumno eliminado (soft): {} {}", alumno.getNombres(), alumno.getApellidos());
    }

    private String generateCodigoEstudiante() {
        int year = java.time.Year.now().getValue();
        long count = alumnoRepository.count() + 1;
        return String.format("%d%05d", year, count);
    }
}
