package pe.edu.colegioricardopalma.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.dto.ResetPasswordRequest;
import pe.edu.colegioricardopalma.dto.UsuarioGestionDto;
import pe.edu.colegioricardopalma.dto.UsuarioGestionRequest;
import pe.edu.colegioricardopalma.entity.*;
import pe.edu.colegioricardopalma.repository.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioGestionService {

    private static final String TIPO_ADMIN = "ADMIN";
    private static final String TIPO_PROFESOR = "PROFESOR";
    private static final String TIPO_PADRE = "PADRE";

    private final UsuarioRepository usuarioRepository;
    private final DocenteRepository docenteRepository;
    private final ApoderadoRepository apoderadoRepository;
    private final AlumnoRepository alumnoRepository;
    private final AlumnoApoderadoRepository alumnoApoderadoRepository;
    private final GradoRepository gradoRepository;
    private final SeccionRepository seccionRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PageResponse<UsuarioGestionDto> findAll(
            String q,
            String tipo,
            String estado,
            UUID gradoId,
            UUID seccionId,
            Pageable pageable
    ) {
        String qNorm = normalize(q);
        String tipoNorm = tipo == null ? "" : tipo.trim().toUpperCase(Locale.ROOT);
        String estadoNorm = estado == null ? "" : estado.trim().toUpperCase(Locale.ROOT);

        List<UsuarioGestionDto> rows = new ArrayList<>();

        if (tipoNorm.isBlank() || TIPO_ADMIN.equals(tipoNorm)) rows.addAll(loadAdmins(qNorm, estadoNorm));
        if (tipoNorm.isBlank() || TIPO_PROFESOR.equals(tipoNorm)) rows.addAll(loadProfesores(qNorm, estadoNorm));
        if (tipoNorm.isBlank() || TIPO_PADRE.equals(tipoNorm)) rows.addAll(loadPadres(qNorm, estadoNorm));

        rows.sort(Comparator
                .comparing((UsuarioGestionDto x) -> safeLower(x.getApellidos()))
                .thenComparing(x -> safeLower(x.getNombres()))
                .thenComparing(x -> safeLower(x.getTipo())));

        int page = Math.max(pageable.getPageNumber(), 0);
        int size = Math.max(pageable.getPageSize(), 1);
        int start = Math.min(page * size, rows.size());
        int end = Math.min(start + size, rows.size());

        List<UsuarioGestionDto> content = rows.subList(start, end);
        PageImpl<UsuarioGestionDto> springPage = new PageImpl<>(content, PageRequest.of(page, size), rows.size());
        return PageResponse.from(springPage);
    }

    @Transactional
    public UsuarioGestionDto create(UsuarioGestionRequest request) {
        String tipo = requireTipo(request.getTipo());
        switch (tipo) {
            case TIPO_ADMIN:
                return createAdmin(request);
            case TIPO_PROFESOR:
                return createProfesor(request);
            case TIPO_PADRE:
                return createPadre(request);
            default:
                throw new IllegalArgumentException("Tipo no soportado: " + tipo);
        }
    }

    @Transactional
    public UsuarioGestionDto update(UUID id, UsuarioGestionRequest request) {
        String tipo = requireTipo(request.getTipo());
        switch (tipo) {
            case TIPO_ADMIN:
                return updateAdmin(id, request);
            case TIPO_PROFESOR:
                return updateProfesor(id, request);
            case TIPO_PADRE:
                return updatePadre(id, request);
            default:
                throw new IllegalArgumentException("Tipo no soportado: " + tipo);
        }
    }

    @Transactional
    public void delete(UUID id, String tipo) {
        String tipoNorm = requireTipo(tipo);
        switch (tipoNorm) {
            case TIPO_ADMIN:
                Usuario admin = usuarioRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Administrador no encontrado: " + id));
                admin.setEstado(Usuario.Estado.ELIMINADO);
                usuarioRepository.save(admin);
                return;
            case TIPO_PROFESOR:
                Docente docente = docenteRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Profesor no encontrado: " + id));
                docente.setEstado(Estado.ELIMINADO);
                if (docente.getUsuario() != null) {
                    docente.getUsuario().setEstado(Usuario.Estado.INACTIVO);
                    usuarioRepository.save(docente.getUsuario());
                }
                docenteRepository.save(docente);
                return;
            case TIPO_PADRE:
                Apoderado apoderado = apoderadoRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Padre no encontrado: " + id));
                apoderado.setEstado(Estado.ELIMINADO);
                if (apoderado.getUsuario() != null) {
                    apoderado.getUsuario().setEstado(Usuario.Estado.INACTIVO);
                    usuarioRepository.save(apoderado.getUsuario());
                }
                apoderadoRepository.save(apoderado);
                return;
            default:
                throw new IllegalArgumentException("Tipo no soportado: " + tipoNorm);
        }
    }

    @Transactional
    public void resetPassword(UUID id, String tipo, ResetPasswordRequest request) {
        if (request == null || isBlank(request.getNewPassword())) {
            throw new IllegalArgumentException("La nueva contraseña es obligatoria");
        }

        String tipoNorm = requireTipo(tipo);
        Usuario usuario;
        switch (tipoNorm) {
            case TIPO_ADMIN:
                usuario = usuarioRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Administrador no encontrado: " + id));
                break;
            case TIPO_PROFESOR:
                Docente docente = docenteRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Profesor no encontrado: " + id));
                if (docente.getUsuario() == null) {
                    throw new IllegalStateException("El profesor no tiene usuario asociado");
                }
                usuario = docente.getUsuario();
                break;
            case TIPO_PADRE:
                Apoderado apoderado = apoderadoRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Padre no encontrado: " + id));
                if (apoderado.getUsuario() == null) {
                    throw new IllegalStateException("El padre no tiene usuario asociado");
                }
                usuario = apoderado.getUsuario();
                break;
            default:
                throw new IllegalArgumentException("Tipo no soportado: " + tipoNorm);
        }

        usuario.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        usuarioRepository.save(usuario);
    }

    private List<UsuarioGestionDto> loadAdmins(String q, String estado) {
        List<UsuarioGestionDto> list = new ArrayList<>();
        for (Usuario u : usuarioRepository.findAll()) {
            if (u.getRol() != Usuario.Rol.ADMIN) continue;
            if (!estado.isBlank() && !u.getEstado().name().equals(estado)) continue;

            UsuarioGestionDto dto = UsuarioGestionDto.builder()
                    .id(u.getId())
                    .tipo(TIPO_ADMIN)
                    .usuarioId(u.getId())
                    .username(u.getUsername())
                    .email(u.getEmail())
                    .estado(u.getEstado().name())
                    .build();

            if (matchesQ(dto, q)) list.add(dto);
        }
        return list;
    }

    private List<UsuarioGestionDto> loadProfesores(String q, String estado) {
        List<UsuarioGestionDto> list = new ArrayList<>();
        for (Docente d : docenteRepository.findAll()) {
            if (!estado.isBlank() && !d.getEstado().name().equals(estado)) continue;

            UsuarioGestionDto dto = UsuarioGestionDto.builder()
                    .id(d.getId())
                    .tipo(TIPO_PROFESOR)
                    .dni(d.getDni())
                    .nombres(d.getNombres())
                    .apellidos(d.getApellidos())
                    .telefono(d.getTelefono())
                    .especialidad(d.getEspecialidad())
                    .estado(d.getEstado().name())
                    .build();

            if (d.getUsuario() != null) {
                dto.setUsuarioId(d.getUsuario().getId());
                dto.setUsername(d.getUsuario().getUsername());
                dto.setEmail(d.getUsuario().getEmail());
                dto.setEstado(d.getUsuario().getEstado().name());
            }

            if (matchesQ(dto, q)) list.add(dto);
        }
        return list;
    }

    private List<UsuarioGestionDto> loadPadres(String q, String estado) {
        List<UsuarioGestionDto> list = new ArrayList<>();
        for (Apoderado a : apoderadoRepository.findAll()) {
            if (!estado.isBlank() && !a.getEstado().name().equals(estado)) continue;

            UsuarioGestionDto dto = UsuarioGestionDto.builder()
                    .id(a.getId())
                    .tipo(TIPO_PADRE)
                    .dni(a.getDni())
                    .nombres(a.getNombres())
                    .apellidos(a.getApellidos())
                    .telefono(a.getTelefono())
                    .direccion(a.getDireccion())
                    .hijosCount(Math.toIntExact(alumnoApoderadoRepository.countByApoderadoId(a.getId())))
                    .estado(a.getEstado().name())
                    .build();

            if (a.getUsuario() != null) {
                dto.setUsuarioId(a.getUsuario().getId());
                dto.setUsername(a.getUsuario().getUsername());
                dto.setEmail(a.getUsuario().getEmail());
                dto.setEstado(a.getUsuario().getEstado().name());
            }

            if (matchesQ(dto, q)) list.add(dto);
        }
        return list;
    }

    

    private UsuarioGestionDto createAdmin(UsuarioGestionRequest request) {
        requireNonBlank(request.getDni(), "El DNI es obligatorio");
        requireNonBlank(request.getDni(), "El DNI es obligatorio");
        requireNonBlank(request.getEmail(), "El email es obligatorio");
        requireNonBlank(request.getPassword(), "La contraseña es obligatoria");

        if (usuarioRepository.existsByUsername(request.getDni().trim())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese DNI");
        }
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }

        Usuario usuario = Usuario.builder()
                .username(request.getDni().trim())
                .email(request.getEmail().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(Usuario.Rol.ADMIN)
                .estado(parseUsuarioEstadoOrDefault(request.getEstado(), Usuario.Estado.ACTIVO))
                .build();

        Usuario saved = usuarioRepository.save(usuario);
        return UsuarioGestionDto.builder()
                .id(saved.getId())
                .tipo(TIPO_ADMIN)
                .usuarioId(saved.getId())
                .dni(saved.getUsername())
                .username(saved.getUsername())
                .email(saved.getEmail())
                .estado(saved.getEstado().name())
                .build();
    }

    private UsuarioGestionDto createProfesor(UsuarioGestionRequest request) {
        requireNonBlank(request.getDni(), "El DNI es obligatorio");
        requireNonBlank(request.getNombres(), "Los nombres son obligatorios");
        requireNonBlank(request.getApellidos(), "Los apellidos son obligatorios");
        requireNonBlank(request.getDni(), "El DNI es obligatorio");
        requireNonBlank(request.getEmail(), "El email es obligatorio");
        requireNonBlank(request.getPassword(), "La contraseña es obligatoria");

        if (docenteRepository.existsByDni(request.getDni())) {
            throw new IllegalArgumentException("Ya existe un docente con ese DNI");
        }
        if (usuarioRepository.existsByUsername(request.getDni().trim())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese DNI");
        }
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }

        Usuario usuario = Usuario.builder()
                .username(request.getDni().trim())
                .email(request.getEmail().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(Usuario.Rol.PROFESOR)
                .estado(parseUsuarioEstadoOrDefault(request.getEstado(), Usuario.Estado.ACTIVO))
                .build();
        Usuario savedUser = usuarioRepository.save(usuario);

        Docente docente = Docente.builder()
                .dni(request.getDni().trim())
                .nombres(request.getNombres().trim())
                .apellidos(request.getApellidos().trim())
                .telefono(trimToNull(request.getTelefono()))
                .email(request.getEmail().trim())
                .especialidad(trimToNull(request.getEspecialidad()))
                .estado(parseEstadoOrDefault(request.getEstado(), Estado.ACTIVO))
                .usuario(savedUser)
                .build();

        Docente saved = docenteRepository.save(docente);
        return toDto(saved);
    }

    private UsuarioGestionDto createPadre(UsuarioGestionRequest request) {
        requireNonBlank(request.getDni(), "El DNI es obligatorio");
        requireNonBlank(request.getNombres(), "Los nombres son obligatorios");
        requireNonBlank(request.getApellidos(), "Los apellidos son obligatorios");
        requireNonBlank(request.getDni(), "El DNI es obligatorio");
        requireNonBlank(request.getEmail(), "El email es obligatorio");
        requireNonBlank(request.getPassword(), "La contraseña es obligatoria");

        if (apoderadoRepository.existsByDni(request.getDni())) {
            throw new IllegalArgumentException("Ya existe un apoderado con ese DNI");
        }
        if (usuarioRepository.existsByUsername(request.getDni().trim())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese DNI");
        }
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }

        Usuario usuario = Usuario.builder()
                .username(request.getDni().trim())
                .email(request.getEmail().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(Usuario.Rol.PADRE)
                .estado(parseUsuarioEstadoOrDefault(request.getEstado(), Usuario.Estado.ACTIVO))
                .build();
        Usuario savedUser = usuarioRepository.save(usuario);

        Apoderado apoderado = Apoderado.builder()
                .dni(request.getDni().trim())
                .nombres(request.getNombres().trim())
                .apellidos(request.getApellidos().trim())
                .telefono(trimToNull(request.getTelefono()))
                .email(request.getEmail().trim())
                .direccion(trimToNull(request.getDireccion()))
                .estado(parseEstadoOrDefault(request.getEstado(), Estado.ACTIVO))
                .usuario(savedUser)
                .build();

        Apoderado saved = apoderadoRepository.save(apoderado);

        if (Boolean.TRUE.equals(request.getCrearHijo())) {
            createAndLinkHijo(saved, request);
        }

        return toDto(saved);
    }

    private UsuarioGestionDto updateAdmin(UUID id, UsuarioGestionRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Administrador no encontrado: " + id));

        requireNonBlank(request.getDni(), "El DNI es obligatorio");
        requireNonBlank(request.getEmail(), "El email es obligatorio");

        String newUsername = request.getDni().trim();
        String newEmail = request.getEmail().trim();

        if (!newUsername.equals(usuario.getUsername()) && usuarioRepository.existsByUsername(newUsername)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese DNI");
        }
        if (!newEmail.equals(usuario.getEmail()) && usuarioRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }

        usuario.setUsername(newUsername);
        usuario.setEmail(newEmail);
        usuario.setEstado(parseUsuarioEstadoOrDefault(request.getEstado(), usuario.getEstado()));
        if (!isBlank(request.getPassword())) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        Usuario saved = usuarioRepository.save(usuario);
        return UsuarioGestionDto.builder()
                .id(saved.getId())
                .tipo(TIPO_ADMIN)
                .usuarioId(saved.getId())
                .username(saved.getUsername())
                .email(saved.getEmail())
                .estado(saved.getEstado().name())
                .build();
    }

    private UsuarioGestionDto updateProfesor(UUID id, UsuarioGestionRequest request) {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profesor no encontrado: " + id));

        requireNonBlank(request.getDni(), "El DNI es obligatorio");
        requireNonBlank(request.getNombres(), "Los nombres son obligatorios");
        requireNonBlank(request.getApellidos(), "Los apellidos son obligatorios");
        requireNonBlank(request.getEmail(), "El email es obligatorio");

        if (!request.getDni().trim().equals(docente.getDni()) && docenteRepository.existsByDni(request.getDni().trim())) {
            throw new IllegalArgumentException("Ya existe un docente con ese DNI");
        }

        Usuario usuario = docente.getUsuario();
        if (usuario == null) {
            usuario = Usuario.builder()
                    .rol(Usuario.Rol.PROFESOR)
                    .passwordHash(passwordEncoder.encode(isBlank(request.getPassword()) ? "Cambio123!" : request.getPassword()))
                    .build();
        }

        String newUsername = request.getDni().trim();
        String newEmail = request.getEmail().trim();

        if (usuario.getUsername() != null && !newUsername.equals(usuario.getUsername()) && usuarioRepository.existsByUsername(newUsername)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese DNI");
        }
        if (usuario.getEmail() != null && !newEmail.equals(usuario.getEmail()) && usuarioRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }

        usuario.setUsername(newUsername);
        usuario.setEmail(newEmail);
        usuario.setEstado(parseUsuarioEstadoOrDefault(request.getEstado(),
                usuario.getEstado() != null ? usuario.getEstado() : Usuario.Estado.ACTIVO));
        if (!isBlank(request.getPassword())) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        Usuario savedUser = usuarioRepository.save(usuario);

        docente.setDni(request.getDni().trim());
        docente.setNombres(request.getNombres().trim());
        docente.setApellidos(request.getApellidos().trim());
        docente.setTelefono(trimToNull(request.getTelefono()));
        docente.setEmail(newEmail);
        docente.setEspecialidad(trimToNull(request.getEspecialidad()));
        docente.setEstado(parseEstadoOrDefault(request.getEstado(), docente.getEstado()));
        docente.setUsuario(savedUser);

        Docente saved = docenteRepository.save(docente);
        return toDto(saved);
    }

    private UsuarioGestionDto updatePadre(UUID id, UsuarioGestionRequest request) {
        Apoderado apoderado = apoderadoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Padre no encontrado: " + id));

        requireNonBlank(request.getDni(), "El DNI es obligatorio");
        requireNonBlank(request.getNombres(), "Los nombres son obligatorios");
        requireNonBlank(request.getApellidos(), "Los apellidos son obligatorios");
        requireNonBlank(request.getEmail(), "El email es obligatorio");

        if (!request.getDni().trim().equals(apoderado.getDni()) && apoderadoRepository.existsByDni(request.getDni().trim())) {
            throw new IllegalArgumentException("Ya existe un apoderado con ese DNI");
        }

        Usuario usuario = apoderado.getUsuario();
        if (usuario == null) {
            usuario = Usuario.builder()
                    .rol(Usuario.Rol.PADRE)
                    .passwordHash(passwordEncoder.encode(isBlank(request.getPassword()) ? "Cambio123!" : request.getPassword()))
                    .build();
        }

        String newUsername = request.getDni().trim();
        String newEmail = request.getEmail().trim();

        if (usuario.getUsername() != null && !newUsername.equals(usuario.getUsername()) && usuarioRepository.existsByUsername(newUsername)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese DNI");
        }
        if (usuario.getEmail() != null && !newEmail.equals(usuario.getEmail()) && usuarioRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }

        usuario.setUsername(newUsername);
        usuario.setEmail(newEmail);
        usuario.setEstado(parseUsuarioEstadoOrDefault(request.getEstado(),
                usuario.getEstado() != null ? usuario.getEstado() : Usuario.Estado.ACTIVO));
        if (!isBlank(request.getPassword())) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        Usuario savedUser = usuarioRepository.save(usuario);

        apoderado.setDni(request.getDni().trim());
        apoderado.setNombres(request.getNombres().trim());
        apoderado.setApellidos(request.getApellidos().trim());
        apoderado.setTelefono(trimToNull(request.getTelefono()));
        apoderado.setEmail(newEmail);
        apoderado.setDireccion(trimToNull(request.getDireccion()));
        apoderado.setEstado(parseEstadoOrDefault(request.getEstado(), apoderado.getEstado()));
        apoderado.setUsuario(savedUser);

        Apoderado saved = apoderadoRepository.save(apoderado);

        if (Boolean.TRUE.equals(request.getCrearHijo())) {
            createAndLinkHijo(saved, request);
        }

        return toDto(saved);
    }

    private void createAndLinkHijo(Apoderado apoderado, UsuarioGestionRequest request) {
        requireNonBlank(request.getHijoDni(), "El DNI del hijo es obligatorio");
        requireNonBlank(request.getHijoNombres(), "Los nombres del hijo son obligatorios");
        requireNonBlank(request.getHijoApellidos(), "Los apellidos del hijo son obligatorios");
        requireNonBlank(request.getHijoCodigoEstudiante(), "El código de estudiante del hijo es obligatorio");
        if (request.getHijoGradoId() == null) {
            throw new IllegalArgumentException("El grado del hijo es obligatorio");
        }
        if (request.getHijoSeccionId() == null) {
            throw new IllegalArgumentException("La sección del hijo es obligatoria");
        }

        if (alumnoRepository.findByDni(request.getHijoDni().trim()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un alumno con ese DNI");
        }

        Grado grado = gradoRepository.findById(request.getHijoGradoId())
                .orElseThrow(() -> new EntityNotFoundException("Grado no encontrado: " + request.getHijoGradoId()));
        Seccion seccion = seccionRepository.findById(request.getHijoSeccionId())
                .orElseThrow(() -> new EntityNotFoundException("Sección no encontrada: " + request.getHijoSeccionId()));

        Alumno alumno = Alumno.builder()
                .dni(request.getHijoDni().trim())
                .codigoEstudiante(request.getHijoCodigoEstudiante().trim())
                .nombres(request.getHijoNombres().trim())
                .apellidos(request.getHijoApellidos().trim())
                .fechaNacimiento(request.getHijoFechaNacimiento())
                .grado(grado)
                .seccion(seccion)
                .estado(Estado.ACTIVO)
                .build();
        Alumno savedAlumno = alumnoRepository.save(alumno);

        alumnoApoderadoRepository.save(AlumnoApoderado.builder()
                .alumno(savedAlumno)
                .apoderado(apoderado)
                .parentesco("PADRE")
                .esPrincipal(true)
                .build());
    }

    private UsuarioGestionDto toDto(Docente d) {
        UsuarioGestionDto dto = UsuarioGestionDto.builder()
                .id(d.getId())
                .tipo(TIPO_PROFESOR)
                .dni(d.getDni())
                .nombres(d.getNombres())
                .apellidos(d.getApellidos())
                .telefono(d.getTelefono())
                .especialidad(d.getEspecialidad())
                .estado(d.getEstado().name())
                .build();
        if (d.getUsuario() != null) {
            dto.setUsuarioId(d.getUsuario().getId());
            dto.setUsername(d.getUsuario().getUsername());
            dto.setEmail(d.getUsuario().getEmail());
            dto.setEstado(d.getUsuario().getEstado().name());
        }
        return dto;
    }

    private UsuarioGestionDto toDto(Apoderado a) {
        UsuarioGestionDto dto = UsuarioGestionDto.builder()
                .id(a.getId())
                .tipo(TIPO_PADRE)
                .dni(a.getDni())
                .nombres(a.getNombres())
                .apellidos(a.getApellidos())
                .telefono(a.getTelefono())
                .direccion(a.getDireccion())
                .estado(a.getEstado().name())
                .hijosCount(Math.toIntExact(alumnoApoderadoRepository.countByApoderadoId(a.getId())))
                .build();
        if (a.getUsuario() != null) {
            dto.setUsuarioId(a.getUsuario().getId());
            dto.setUsername(a.getUsuario().getUsername());
            dto.setEmail(a.getUsuario().getEmail());
            dto.setEstado(a.getUsuario().getEstado().name());
        }
        return dto;
    }

    private boolean matchesQ(UsuarioGestionDto dto, String q) {
        if (q.isBlank()) return true;
        return contains(dto.getTipo(), q)
                || contains(dto.getUsername(), q)
                || contains(dto.getEmail(), q)
                || contains(dto.getDni(), q)
                || contains(dto.getCodigoEstudiante(), q)
                || contains(dto.getNombres(), q)
                || contains(dto.getApellidos(), q)
                || contains(dto.getTelefono(), q)
                || contains(dto.getDireccion(), q)
                || contains(dto.getEspecialidad(), q)
                || contains(dto.getGradoNombre(), q)
                || contains(dto.getSeccionNombre(), q);
    }

    private boolean contains(String candidate, String q) {
        return candidate != null && candidate.toLowerCase(Locale.ROOT).contains(q);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String requireTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("El campo tipo es obligatorio");
        }
        String up = tipo.trim().toUpperCase(Locale.ROOT);
        if (!TIPO_ADMIN.equals(up) && !TIPO_PROFESOR.equals(up) && !TIPO_PADRE.equals(up)) {
            throw new IllegalArgumentException("Tipo inválido: " + tipo);
        }
        return up;
    }

    private Estado parseEstadoOrDefault(String value, Estado defaultValue) {
        if (isBlank(value)) return defaultValue;
        try {
            return Estado.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Estado inválido: " + value);
        }
    }

    private Usuario.Estado parseUsuarioEstadoOrDefault(String value, Usuario.Estado defaultValue) {
        if (isBlank(value)) return defaultValue;
        try {
            return Usuario.Estado.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Estado de usuario inválido: " + value);
        }
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void requireNonBlank(String value, String message) {
        if (isBlank(value)) throw new IllegalArgumentException(message);
    }

}
