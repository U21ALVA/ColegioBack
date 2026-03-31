package pe.edu.colegioricardopalma.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.Pension;
import pe.edu.colegioricardopalma.entity.PensionEstado;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PensionRepository extends JpaRepository<Pension, UUID> {

    @Query("SELECT p FROM Pension p " +
           "JOIN FETCH p.alumno a " +
           "JOIN FETCH p.anioEscolar ae " +
           "WHERE p.id = :id")
    Optional<Pension> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT p FROM Pension p WHERE p.alumno.id = :alumnoId ORDER BY p.mes")
    List<Pension> findByAlumnoId(@Param("alumnoId") UUID alumnoId);

    @Query("SELECT p FROM Pension p " +
           "JOIN FETCH p.alumno a " +
           "JOIN FETCH p.anioEscolar ae " +
           "WHERE p.alumno.id = :alumnoId " +
           "AND p.anioEscolar.id = :anioEscolarId " +
           "ORDER BY p.mes")
    List<Pension> findByAlumnoIdAndAnioEscolarId(
            @Param("alumnoId") UUID alumnoId,
            @Param("anioEscolarId") UUID anioEscolarId);

    @Query("SELECT p FROM Pension p " +
           "JOIN FETCH p.alumno a " +
           "WHERE p.alumno.id = :alumnoId " +
           "AND p.estado = :estado " +
           "ORDER BY p.mes")
    List<Pension> findByAlumnoIdAndEstado(
            @Param("alumnoId") UUID alumnoId,
            @Param("estado") PensionEstado estado);

    @Query("SELECT p FROM Pension p WHERE p.estado = :estado")
    List<Pension> findByEstado(@Param("estado") PensionEstado estado);

    @Query("SELECT p FROM Pension p " +
           "JOIN FETCH p.alumno a " +
           "JOIN FETCH p.anioEscolar ae " +
           "WHERE p.anioEscolar.id = :anioEscolarId " +
           "AND (:mes IS NULL OR p.mes = :mes) " +
           "AND (:estado IS NULL OR p.estado = :estado)")
    Page<Pension> findWithFilters(
            @Param("anioEscolarId") UUID anioEscolarId,
            @Param("mes") Integer mes,
            @Param("estado") PensionEstado estado,
            Pageable pageable);

    @Query("SELECT p FROM Pension p " +
           "JOIN FETCH p.alumno a " +
           "LEFT JOIN a.grado g " +
           "JOIN FETCH p.anioEscolar ae " +
           "WHERE ae.activo = true " +
           "AND (:mes IS NULL OR p.mes = :mes) " +
           "AND (:estado IS NULL OR p.estado = :estado) " +
           "AND (:gradoId IS NULL OR a.grado.id = :gradoId)")
    Page<Pension> findActiveWithFilters(
            @Param("mes") Integer mes,
            @Param("estado") PensionEstado estado,
            @Param("gradoId") UUID gradoId,
            Pageable pageable);

    boolean existsByAlumnoIdAndAnioEscolarIdAndMes(UUID alumnoId, UUID anioEscolarId, Integer mes);

    Optional<Pension> findByAlumnoIdAndAnioEscolarIdAndMes(UUID alumnoId, UUID anioEscolarId, Integer mes);

    @Query("SELECT COUNT(p) FROM Pension p WHERE p.anioEscolar.id = :anioEscolarId AND p.estado = :estado")
    Long countByAnioEscolarIdAndEstado(
            @Param("anioEscolarId") UUID anioEscolarId,
            @Param("estado") PensionEstado estado);

    @Query("SELECT COALESCE(SUM(p.montoFinal), 0) FROM Pension p " +
           "WHERE p.anioEscolar.id = :anioEscolarId AND p.estado = 'PAGADO'")
    java.math.BigDecimal sumMontoFinalPagadoByAnioEscolar(@Param("anioEscolarId") UUID anioEscolarId);

    @Query("SELECT p FROM Pension p " +
           "JOIN FETCH p.alumno a " +
           "WHERE a.id IN :alumnoIds " +
           "AND p.estado IN ('PENDIENTE', 'VENCIDO', 'PARCIAL') " +
           "ORDER BY a.apellidos, a.nombres, p.mes")
    List<Pension> findPendientesByAlumnoIds(@Param("alumnoIds") List<UUID> alumnoIds);
}
