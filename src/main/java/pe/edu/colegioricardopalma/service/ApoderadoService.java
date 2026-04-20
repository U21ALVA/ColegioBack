package pe.edu.colegioricardopalma.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.AlumnoApoderadoDto;
import pe.edu.colegioricardopalma.dto.ApoderadoDto;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.entity.*;
import pe.edu.colegioricardopalma.repository.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApoderadoService {

    private static final Logger log = LoggerFactory.getLogger(ApoderadoService.class);

    private final ApoderadoRepository apoderadoRepository;
    private final AlumnoRepository alumnoRepository;
    private final AlumnoApoderadoRepository alumnoApoderadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ApoderadoService(
            ApoderadoRepository apoderadoRepository,
            AlumnoRepository alumnoRepository,
            AlumnoApoderadoRepository alumnoApoderadoRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.apoderadoRepository = apoderadoRepository;
        this.alumnoRepository = alumnoRepository;
        this.alumnoApoderadoRepository = alumnoApoderadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<ApoderadoDto> findAll() {
        return apoderadoRepository.findActivos()
                .stream()
                .map(ApoderadoDto::fromEntity)
                .collect(Collectors.toList());
    }

    public PageResponse<ApoderadoDto> findAllPaginated(Pageable pageable) {
        Page<ApoderadoDto> page = apoderadoRepository.findActivos(pageable)
                .map(ApoderadoDto::fromEntity);
        return PageResponse.from(page);
    }

    public PageResponse<ApoderadoDto> search(String search, Pageable pageable) {
        Page<ApoderadoDto> page = apoderadoRepository.searchApoderados(
                search != null ? search : "",
                pageable
        ).map(ApoderadoDto::fromEntity);
        return PageResponse.from(page);
    }

    public ApoderadoDto findById(UUID id) {
        Apoderado apoderado = apoderadoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Apoderado no encontrado: " + id));
        
        ApoderadoDto dto = ApoderadoDto.fromEntity(apoderado);
        
        // Load hijos
        List<AlumnoApoderadoDto> hijos = alumnoApoderadoRepository.findByApoderadoIdWithAlumno(id)
                .stream()
                .map(AlumnoApoderadoDto::fromEntity)
                .collect(Collectors.toList());
        dto.setHijos(hijos);
        
        return dto;
    }

    public ApoderadoDto findByDni(String dni) {
        Apoderado apoderado = apoderadoRepository.findByDni(dni)
                .orElseThrow(() -> new EntityNotFoundException("Apoderado no encontrado con DNI: " + dni));
        return ApoderadoDto.fromEntity(apoderado);
    }

    public Long countActivos() {
        return apoderadoRepository.countActivos();
    }

    @Transactional
    public ApoderadoDto create(ApoderadoDto dto) {
        if (apoderadoRepository.existsByDni(dto.getDni())) {
            throw new IllegalArgumentException("Ya existe un apoderado con el DNI: " + dto.getDni());
        }

        if (dto.getEmail() != null && apoderadoRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un apoderado con el email: " + dto.getEmail());
        }

        Apoderado apoderado = dto.toEntity();

        // Create user account if requested
        if (Boolean.TRUE.equals(dto.getCrearUsuario()) && dto.getPassword() != null) {
            Usuario usuario = createUsuarioForApoderado(dto);
            apoderado.setUsuario(usuario);
        }

        Apoderado saved = apoderadoRepository.save(apoderado);
        log.info("Apoderado creado: {} {}", saved.getNombres(), saved.getApellidos());
        
        return ApoderadoDto.fromEntity(saved);
    }

    @Transactional
    public ApoderadoDto update(UUID id, ApoderadoDto dto) {
        Apoderado apoderado = apoderadoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Apoderado no encontrado: " + id));

        // Check for duplicate DNI
        if (!apoderado.getDni().equals(dto.getDni()) && apoderadoRepository.existsByDni(dto.getDni())) {
            throw new IllegalArgumentException("Ya existe un apoderado con el DNI: " + dto.getDni());
        }

        // Check for duplicate email
        if (dto.getEmail() != null && !dto.getEmail().equals(apoderado.getEmail()) 
                && apoderadoRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un apoderado con el email: " + dto.getEmail());
        }

        apoderado.setDni(dto.getDni());
        apoderado.setNombres(dto.getNombres());
        apoderado.setApellidos(dto.getApellidos());
        apoderado.setTelefono(dto.getTelefono());
        apoderado.setEmail(dto.getEmail());
        apoderado.setDireccion(dto.getDireccion());
        if (dto.getEstado() != null) {
            apoderado.setEstado(dto.getEstado());
        }

        // Create user account if requested and doesn't exist
        if (Boolean.TRUE.equals(dto.getCrearUsuario()) && apoderado.getUsuario() == null && dto.getPassword() != null) {
            Usuario usuario = createUsuarioForApoderado(dto);
            apoderado.setUsuario(usuario);
        }
        
        Apoderado saved = apoderadoRepository.save(apoderado);
        log.info("Apoderado actualizado: {} {}", saved.getNombres(), saved.getApellidos());
        
        return ApoderadoDto.fromEntity(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Apoderado apoderado = apoderadoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Apoderado no encontrado: " + id));

        apoderado.setEstado(Estado.ELIMINADO);
        
        // Also deactivate user account if exists
        if (apoderado.getUsuario() != null) {
            apoderado.getUsuario().setEstado(Usuario.Estado.INACTIVO);
            usuarioRepository.save(apoderado.getUsuario());
        }
        
        apoderadoRepository.save(apoderado);
        log.info("Apoderado eliminado (soft): {} {}", apoderado.getNombres(), apoderado.getApellidos());
    }

    @Transactional
    public AlumnoApoderadoDto linkAlumno(UUID apoderadoId, AlumnoApoderadoDto dto) {
        Apoderado apoderado = apoderadoRepository.findById(apoderadoId)
                .orElseThrow(() -> new EntityNotFoundException("Apoderado no encontrado: " + apoderadoId));

        Alumno alumno = alumnoRepository.findById(dto.getAlumnoId())
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado: " + dto.getAlumnoId()));

        if (alumnoApoderadoRepository.existsByAlumnoIdAndApoderadoId(alumno.getId(), apoderado.getId())) {
            throw new IllegalArgumentException("El alumno ya está vinculado a este apoderado");
        }

        AlumnoApoderado relation = AlumnoApoderado.builder()
                .alumno(alumno)
                .apoderado(apoderado)
                .parentesco(dto.getParentesco())
                .esPrincipal(dto.getEsPrincipal() != null ? dto.getEsPrincipal() : false)
                .build();

        AlumnoApoderado saved = alumnoApoderadoRepository.save(relation);
        log.info("Alumno {} vinculado a apoderado {}", alumno.getNombreCompleto(), apoderado.getNombreCompleto());
        
        return AlumnoApoderadoDto.fromEntity(saved);
    }

    @Transactional
    public void unlinkAlumno(UUID apoderadoId, UUID alumnoId) {
        if (!alumnoApoderadoRepository.existsByAlumnoIdAndApoderadoId(alumnoId, apoderadoId)) {
            throw new EntityNotFoundException("No existe la relación entre el alumno y el apoderado");
        }

        alumnoApoderadoRepository.deleteByAlumnoIdAndApoderadoId(alumnoId, apoderadoId);
        log.info("Alumno {} desvinculado del apoderado {}", alumnoId, apoderadoId);
    }

    private Usuario createUsuarioForApoderado(ApoderadoDto dto) {
        String username = generateUsername(dto.getNombres(), dto.getApellidos());
        
        // Ensure username is unique
        int counter = 1;
        String baseUsername = username;
        while (usuarioRepository.existsByUsername(username)) {
            username = baseUsername + counter++;
        }

        Usuario usuario = Usuario.builder()
                .username(username)
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .rol(Usuario.Rol.PADRE)
                .estado(Usuario.Estado.ACTIVO)
                .build();

        return usuarioRepository.save(usuario);
    }

    private String generateUsername(String nombres, String apellidos) {
        String nombre = nombres.split(" ")[0].toLowerCase();
        String apellido = apellidos.split(" ")[0].toLowerCase();
        return nombre.charAt(0) + apellido;
    }
}
