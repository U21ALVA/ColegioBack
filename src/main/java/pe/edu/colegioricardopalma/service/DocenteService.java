package pe.edu.colegioricardopalma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.DocenteDto;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.entity.Docente;
import pe.edu.colegioricardopalma.entity.Usuario;
import pe.edu.colegioricardopalma.repository.DocenteRepository;
import pe.edu.colegioricardopalma.repository.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocenteService {

    private final DocenteRepository docenteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<DocenteDto> findAll() {
        return docenteRepository.findActivos()
                .stream()
                .map(DocenteDto::fromEntity)
                .collect(Collectors.toList());
    }

    public PageResponse<DocenteDto> findAllPaginated(Pageable pageable) {
        Page<DocenteDto> page = docenteRepository.findActivos(pageable)
                .map(DocenteDto::fromEntity);
        return PageResponse.from(page);
    }

    public PageResponse<DocenteDto> search(String search, Pageable pageable) {
        Page<DocenteDto> page = docenteRepository.searchDocentes(
                search != null ? search : "",
                pageable
        ).map(DocenteDto::fromEntity);
        return PageResponse.from(page);
    }

    public DocenteDto findById(UUID id) {
        Docente docente = docenteRepository.findByIdWithUsuario(id)
                .orElseThrow(() -> new EntityNotFoundException("Docente no encontrado: " + id));
        return DocenteDto.fromEntity(docente);
    }

    public DocenteDto findByDni(String dni) {
        Docente docente = docenteRepository.findByDni(dni)
                .orElseThrow(() -> new EntityNotFoundException("Docente no encontrado con DNI: " + dni));
        return DocenteDto.fromEntity(docente);
    }

    public Long countActivos() {
        return docenteRepository.countActivos();
    }

    @Transactional
    public DocenteDto create(DocenteDto dto) {
        if (docenteRepository.existsByDni(dto.getDni())) {
            throw new IllegalArgumentException("Ya existe un docente con el DNI: " + dto.getDni());
        }

        if (dto.getEmail() != null && docenteRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un docente con el email: " + dto.getEmail());
        }

        Docente docente = dto.toEntity();

        // Create user account if requested
        if (Boolean.TRUE.equals(dto.getCrearUsuario()) && dto.getPassword() != null) {
            Usuario usuario = createUsuarioForDocente(dto);
            docente.setUsuario(usuario);
        }

        Docente saved = docenteRepository.save(docente);
        log.info("Docente creado: {} {}", saved.getNombres(), saved.getApellidos());
        
        return DocenteDto.fromEntity(saved);
    }

    @Transactional
    public DocenteDto update(UUID id, DocenteDto dto) {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Docente no encontrado: " + id));

        // Check for duplicate DNI
        if (!docente.getDni().equals(dto.getDni()) && docenteRepository.existsByDni(dto.getDni())) {
            throw new IllegalArgumentException("Ya existe un docente con el DNI: " + dto.getDni());
        }

        // Check for duplicate email
        if (dto.getEmail() != null && !dto.getEmail().equals(docente.getEmail()) 
                && docenteRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un docente con el email: " + dto.getEmail());
        }

        docente.setDni(dto.getDni());
        docente.setNombres(dto.getNombres());
        docente.setApellidos(dto.getApellidos());
        docente.setTelefono(dto.getTelefono());
        docente.setEmail(dto.getEmail());
        docente.setEspecialidad(dto.getEspecialidad());
        if (dto.getEstado() != null) {
            docente.setEstado(dto.getEstado());
        }

        // Create user account if requested and doesn't exist
        if (Boolean.TRUE.equals(dto.getCrearUsuario()) && docente.getUsuario() == null && dto.getPassword() != null) {
            Usuario usuario = createUsuarioForDocente(dto);
            docente.setUsuario(usuario);
        }
        
        Docente saved = docenteRepository.save(docente);
        log.info("Docente actualizado: {} {}", saved.getNombres(), saved.getApellidos());
        
        return DocenteDto.fromEntity(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Docente no encontrado: " + id));

        docente.setEstado(pe.edu.colegioricardopalma.entity.Estado.ELIMINADO);
        
        // Also deactivate user account if exists
        if (docente.getUsuario() != null) {
            docente.getUsuario().setEstado(Usuario.Estado.INACTIVO);
            usuarioRepository.save(docente.getUsuario());
        }
        
        docenteRepository.save(docente);
        log.info("Docente eliminado (soft): {} {}", docente.getNombres(), docente.getApellidos());
    }

    private Usuario createUsuarioForDocente(DocenteDto dto) {
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
                .rol(Usuario.Rol.PROFESOR)
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
