package pe.edu.colegioricardopalma.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.entity.Apoderado;
import pe.edu.colegioricardopalma.entity.Docente;
import pe.edu.colegioricardopalma.entity.Estado;
import pe.edu.colegioricardopalma.entity.Usuario;
import pe.edu.colegioricardopalma.repository.ApoderadoRepository;
import pe.edu.colegioricardopalma.repository.DocenteRepository;
import pe.edu.colegioricardopalma.repository.UsuarioRepository;

@Component
@RequiredArgsConstructor
public class BootstrapUsersRunner implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "123456";

    private final UsuarioRepository usuarioRepository;
    private final DocenteRepository docenteRepository;
    private final ApoderadoRepository apoderadoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) {
        ensurePostgresEnumDomains();

        // Base users requested by product owner after clean reset
        upsertUsuario("11111111", "admin11111111@local.test", Usuario.Rol.ADMIN);
        Usuario usuarioPadre = upsertUsuario("22222222", "padre22222222@local.test", Usuario.Rol.PADRE);
        Usuario usuarioDocente = upsertUsuario("33333333", "docente33333333@local.test", Usuario.Rol.PROFESOR);

        Docente docente = docenteRepository.findByDni("33333333")
                .orElseGet(() -> Docente.builder()
                        .dni("33333333")
                        .nombres("Docente")
                        .apellidos("Base")
                        .build());
        docente.setNombres("Docente");
        docente.setApellidos("Base");
        docente.setEmail(usuarioDocente.getEmail());
        docente.setEstado(Estado.ACTIVO);
        docente.setUsuario(usuarioDocente);
        docenteRepository.save(docente);

        Apoderado apoderado = apoderadoRepository.findByDni("22222222")
                .orElseGet(() -> Apoderado.builder()
                        .dni("22222222")
                        .nombres("Padre")
                        .apellidos("Base")
                        .build());
        apoderado.setNombres("Padre");
        apoderado.setApellidos("Base");
        apoderado.setEmail(usuarioPadre.getEmail());
        apoderado.setEstado(Estado.ACTIVO);
        apoderado.setUsuario(usuarioPadre);
        apoderadoRepository.save(apoderado);
    }

    private Usuario upsertUsuario(String dni, String email, Usuario.Rol rol) {
        Usuario usuario = usuarioRepository.findByUsername(dni)
                .or(() -> usuarioRepository.findByEmail(email))
                .orElseGet(Usuario::new);

        usuario.setUsername(dni);
        usuario.setEmail(email);
        usuario.setRol(rol);
        usuario.setEstado(Usuario.Estado.ACTIVO);
        usuario.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        return usuarioRepository.save(usuario);
    }

    private void ensurePostgresEnumDomains() {
        jdbcTemplate.execute("""
                DO $$
                BEGIN
                  IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'estado_tipo')
                     AND NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'estado') THEN
                    CREATE DOMAIN estado AS estado_tipo;
                  END IF;

                  IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'rol_tipo')
                     AND NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'rol') THEN
                    CREATE DOMAIN rol AS rol_tipo;
                  END IF;

                  IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'nivel_tipo')
                     AND NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'nivel') THEN
                    CREATE DOMAIN nivel AS nivel_tipo;
                  END IF;

                  IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'pension_estado')
                     AND NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'pensionestado') THEN
                    CREATE DOMAIN pensionestado AS pension_estado;
                  END IF;

                  IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'pago_estado')
                     AND NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'pagoestado') THEN
                    CREATE DOMAIN pagoestado AS pago_estado;
                  END IF;
                END
                $$;
                """);
    }
}
