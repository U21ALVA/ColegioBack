package pe.edu.colegioricardopalma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.RefreshToken;
import pe.edu.colegioricardopalma.entity.Usuario;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenAndRevocadoFalse(String token);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revocado = true WHERE r.usuario = :usuario")
    void revokeAllByUsuario(Usuario usuario);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiraAt < :now OR r.revocado = true")
    void deleteExpiredTokens(LocalDateTime now);
}
