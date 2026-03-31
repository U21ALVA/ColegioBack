package pe.edu.colegioricardopalma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import pe.edu.colegioricardopalma.repository.RefreshTokenRepository;
import pe.edu.colegioricardopalma.repository.UsuarioRepository;
import pe.edu.colegioricardopalma.security.CustomUserDetailsService;
import pe.edu.colegioricardopalma.security.JwtService;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    @Transactional
    public LoginResponse login(LoginRequest request, String ip, String userAgent) {
        // Find user
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    auditoriaService.registrarLoginFallido(request.getUsername(), ip, userAgent);
                    return new BadCredentialsException("Credenciales inválidas");
                });

        // Check user status
        if (usuario.getEstado() != Usuario.Estado.ACTIVO) {
            auditoriaService.registrarLoginFallido(request.getUsername(), ip, userAgent);
            throw new BadCredentialsException("Usuario inactivo");
        }

        // Verify password using BCrypt
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            auditoriaService.registrarLoginFallido(request.getUsername(), ip, userAgent);
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
}
