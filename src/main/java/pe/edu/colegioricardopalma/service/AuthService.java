package pe.edu.colegioricardopalma.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.LoginRequest;
import pe.edu.colegioricardopalma.dto.LoginResponse;
import pe.edu.colegioricardopalma.dto.RefreshTokenRequest;
import pe.edu.colegioricardopalma.dto.UserInfoDto;
import pe.edu.colegioricardopalma.entity.RefreshToken;
import pe.edu.colegioricardopalma.entity.Usuario;
import pe.edu.colegioricardopalma.repository.ApoderadoRepository;
import pe.edu.colegioricardopalma.repository.DocenteRepository;
import pe.edu.colegioricardopalma.repository.RefreshTokenRepository;
import pe.edu.colegioricardopalma.repository.UsuarioRepository;
import pe.edu.colegioricardopalma.security.CustomUserDetailsService;
import pe.edu.colegioricardopalma.security.JwtService;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;
    private final DocenteRepository docenteRepository;
    private final ApoderadoRepository apoderadoRepository;

    public AuthService(
            UsuarioRepository usuarioRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            AuditoriaService auditoriaService,
            DocenteRepository docenteRepository,
            ApoderadoRepository apoderadoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
        this.docenteRepository = docenteRepository;
        this.apoderadoRepository = apoderadoRepository;
    }

    @Transactional
    public LoginResponse login(LoginRequest request, String ip, String userAgent) {
        // Find user
        Usuario usuario = resolveUsuarioByDni(request.getDni())
                .orElseThrow(() -> {
                    auditoriaService.registrarLoginFallido(request.getDni(), ip, userAgent);
                    return new BadCredentialsException("Credenciales inválidas");
                });

        // Check user status
        if (usuario.getEstado() != Usuario.Estado.ACTIVO) {
            auditoriaService.registrarLoginFallido(request.getDni(), ip, userAgent);
            throw new BadCredentialsException("Usuario inactivo");
        }

        // Verify password using BCrypt
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            auditoriaService.registrarLoginFallido(request.getDni(), ip, userAgent);
            throw new BadCredentialsException("Credenciales inválidas");
        }

        // Generate tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getUsername());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Revoke existing refresh tokens
        refreshTokenRepository.revokeAllByUsuario(usuario);

        // Save new refresh token
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .usuario(usuario)
                .token(refreshToken)
                .expiraAt(jwtService.getRefreshTokenExpiration())
                .revocado(false)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        // Update last access
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        // Log successful login
        auditoriaService.registrarAcceso(usuario, AuditoriaService.ACCION_LOGIN, ip, userAgent);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresAt(jwtService.getAccessTokenExpiration())
                .user(UserInfoDto.fromUsuario(usuario))
                .build();
    }

    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request, String ip, String userAgent) {
        String requestRefreshToken = request.getRefreshToken();

        // Validate the refresh token exists and is not revoked
        RefreshToken storedToken = refreshTokenRepository.findByTokenAndRevocadoFalse(requestRefreshToken)
                .orElseThrow(() -> new BadCredentialsException("Refresh token inválido o revocado"));

        // Check if expired
        if (storedToken.getExpiraAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new BadCredentialsException("Refresh token expirado");
        }

        // Validate JWT signature
        if (!jwtService.validateToken(requestRefreshToken)) {
            throw new BadCredentialsException("Refresh token inválido");
        }

        Usuario usuario = storedToken.getUsuario();

        // Check user is still active
        if (usuario.getEstado() != Usuario.Estado.ACTIVO) {
            throw new BadCredentialsException("Usuario inactivo");
        }

        // Generate new tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getUsername());
        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        // Revoke old token and save new one
        storedToken.setRevocado(true);
        refreshTokenRepository.save(storedToken);

        RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                .usuario(usuario)
                .token(newRefreshToken)
                .expiraAt(jwtService.getRefreshTokenExpiration())
                .revocado(false)
                .build();
        refreshTokenRepository.save(newRefreshTokenEntity);

        // Log refresh
        auditoriaService.registrarAcceso(usuario, AuditoriaService.ACCION_REFRESH_TOKEN, ip, userAgent);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresAt(jwtService.getAccessTokenExpiration())
                .user(UserInfoDto.fromUsuario(usuario))
                .build();
    }

    @Transactional
    public void logout(String refreshToken, String ip, String userAgent) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenAndRevocadoFalse(refreshToken)
                .orElse(null);

        if (storedToken != null) {
            Usuario usuario = storedToken.getUsuario();
            
            // Revoke all user's tokens
            refreshTokenRepository.revokeAllByUsuario(usuario);
            
            // Log logout
            auditoriaService.registrarAcceso(usuario, AuditoriaService.ACCION_LOGOUT, ip, userAgent);
        }
    }

    public UserInfoDto getCurrentUser(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));
        
        return UserInfoDto.fromUsuario(usuario);
    }

    private java.util.Optional<Usuario> resolveUsuarioByDni(String dni) {
        if (dni == null || dni.isBlank()) {
            return java.util.Optional.empty();
        }
        String dniNorm = dni.trim();

        // 1) Prioridad a usuario por username (DNI): evita tomar perfiles legacy
        //    (docente/apoderado antiguos) que pueden estar inactivos y provocar 401/403 masivos.
        // 2) Fallback por perfil si no existe username directo.
        return usuarioRepository.findByUsername(dniNorm)
                .or(() -> docenteRepository.findByDni(dniNorm)
                        .map(d -> d.getUsuario())
                        .filter(java.util.Objects::nonNull))
                .or(() -> apoderadoRepository.findByDni(dniNorm)
                        .map(a -> a.getUsuario())
                        .filter(java.util.Objects::nonNull));
    }
}
