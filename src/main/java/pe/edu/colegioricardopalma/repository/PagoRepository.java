package pe.edu.colegioricardopalma.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.Pago;
import pe.edu.colegioricardopalma.entity.PagoEstado;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PagoRepository extends JpaRepository<Pago, UUID> {

    @Query("SELECT p FROM Pago p JOIN FETCH p.pension pe JOIN FETCH pe.alumno WHERE p.id = :id")
    Optional<Pago> findByIdWithDetails(@Param("id") UUID id);

    List<Pago> findByPensionId(UUID pensionId);

    Optional<Pago> findByStripeCheckoutSessionId(String sessionId);

    Optional<Pago> findByStripePaymentIntentId(String paymentIntentId);

    @Query(value = "SELECT p.* FROM pago p WHERE p.estado = CAST(:estado AS pago_estado)", nativeQuery = true)
    List<Pago> findByEstado(@Param("estado") String estado);

    @Query("SELECT p FROM Pago p " +
           "JOIN FETCH p.pension pe " +
           "JOIN FETCH pe.alumno a " +
           "WHERE p.fechaPago BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY p.fechaPago DESC")
    Page<Pago> findByFechaRangeAndEstado(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);

    @Query("SELECT p FROM Pago p " +
           "JOIN FETCH p.pension pe " +
           "JOIN FETCH pe.alumno a " +
           "JOIN FETCH pe.anioEscolar ae " +
           "WHERE ae.activo = true " +
           "ORDER BY p.createdAt DESC")
    Page<Pago> findActive(Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p " +
           "WHERE p.estado = 'COMPLETADO' " +
           "AND p.fechaPago BETWEEN :fechaInicio AND :fechaFin")
    java.math.BigDecimal sumMontoByFechaRange(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    @Query(value = "SELECT COUNT(p.id) FROM pago p JOIN pension pe ON pe.id = p.pension_id " +
            "WHERE p.estado = CAST('COMPLETADO' AS pago_estado) AND pe.anio_escolar_id = :anioEscolarId",
            nativeQuery = true)
    Long countCompletadosByAnioEscolar(@Param("anioEscolarId") UUID anioEscolarId);

    @Query(value = "SELECT COALESCE(SUM(p.monto), 0) FROM pago p JOIN pension pe ON pe.id = p.pension_id " +
            "WHERE p.estado = CAST('COMPLETADO' AS pago_estado) AND pe.anio_escolar_id = :anioEscolarId",
            nativeQuery = true)
    java.math.BigDecimal sumMontoCompletadoByAnioEscolar(@Param("anioEscolarId") UUID anioEscolarId);
}
