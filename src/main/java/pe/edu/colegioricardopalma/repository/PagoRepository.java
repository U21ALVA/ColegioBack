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

    @Query("SELECT p FROM Pago p WHERE p.estado = :estado")
    List<Pago> findByEstado(@Param("estado") PagoEstado estado);

    @Query("SELECT p FROM Pago p " +
           "JOIN FETCH p.pension pe " +
           "JOIN FETCH pe.alumno a " +
           "WHERE p.fechaPago BETWEEN :fechaInicio AND :fechaFin " +
           "AND (:estado IS NULL OR p.estado = :estado) " +
           "ORDER BY p.fechaPago DESC")
    Page<Pago> findByFechaRangeAndEstado(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            @Param("estado") PagoEstado estado,
            Pageable pageable);

    @Query("SELECT p FROM Pago p " +
           "JOIN FETCH p.pension pe " +
           "JOIN FETCH pe.alumno a " +
           "JOIN FETCH pe.anioEscolar ae " +
           "WHERE ae.activo = true " +
           "AND (:estado IS NULL OR p.estado = :estado) " +
           "ORDER BY p.createdAt DESC")
    Page<Pago> findActiveWithFilters(
            @Param("estado") PagoEstado estado,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p " +
           "WHERE p.estado = 'COMPLETADO' " +
           "AND p.fechaPago BETWEEN :fechaInicio AND :fechaFin")
    java.math.BigDecimal sumMontoByFechaRange(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT COUNT(p) FROM Pago p WHERE p.estado = :estado AND p.pension.anioEscolar.id = :anioEscolarId")
    Long countByEstadoAndAnioEscolar(
            @Param("estado") PagoEstado estado,
            @Param("anioEscolarId") UUID anioEscolarId);

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p " +
           "WHERE p.estado = 'COMPLETADO' " +
           "AND p.pension.anioEscolar.id = :anioEscolarId")
    java.math.BigDecimal sumMontoCompletadoByAnioEscolar(@Param("anioEscolarId") UUID anioEscolarId);
}
