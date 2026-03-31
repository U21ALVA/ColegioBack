package pe.edu.colegioricardopalma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.TelegramVinculacion;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TelegramVinculacionRepository extends JpaRepository<TelegramVinculacion, UUID> {

    Optional<TelegramVinculacion> findFirstByApoderadoIdOrderByCreatedAtDesc(UUID apoderadoId);

    Optional<TelegramVinculacion> findByCodigoVerificacion(String codigoVerificacion);

    Optional<TelegramVinculacion> findByTelegramChatId(Long telegramChatId);

    @Query("SELECT tv FROM TelegramVinculacion tv JOIN FETCH tv.apoderado WHERE tv.telegramChatId = :chatId")
    Optional<TelegramVinculacion> findByTelegramChatIdWithApoderado(@Param("chatId") Long chatId);
}
