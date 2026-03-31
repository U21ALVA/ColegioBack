package pe.edu.colegioricardopalma.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.Beca;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BecaRepository extends JpaRepository<Beca, UUID> {

    @Query("SELECT b FROM Beca b JOIN FETCH b.alumno a JOIN FETCH b.anioEscolar ae WHERE b.id = :id")
    Optional<Beca> findByIdWithDetails(@Param("id") UUID id);

    List<Beca> findByAlumnoId(UUID alumnoId);

    @Query("SELECT b FROM Beca b " +
           "JOIN FETCH b.alumno a " +
           "JOIN FETCH b.anioEscolar ae " +
           "WHERE b.alumno.id = :alumnoId " +
           "AND b.anioEscolar.id = :anioEscolarId " +
           "AND b.vigente = true")
    List<Beca> findVigentesByAlumnoAndAnioEscolar(
            @Param("alumnoId") UUID alumnoId,
            @Param("anioEscolarId") UUID anioEscolarId);

    @Query("SELECT b FROM Beca b " +
           "JOIN FETCH b.alumno a " +
           "JOIN FETCH b.anioEscolar ae " +
           "WHERE b.anioEscolar.id = :anioEscolarId")
    Page<Beca> findByAnioEscolarId(@Param("anioEscolarId") UUID anioEscolarId, Pageable pageable);

    @Query("SELECT b FROM Beca b " +
           "JOIN FETCH b.alumno a " +
           "JOIN FETCH b.anioEscolar ae " +
           "WHERE ae.activo = true " +
           "AND (:tipo IS NULL OR b.tipo = :tipo) " +
           "AND (:vigente IS NULL OR b.vigente = :vigente)")
    Page<Beca> findActiveWithFilters(
            @Param("tipo") String tipo,
            @Param("vigente") Boolean vigente,
            Pageable pageable);

    boolean existsByAlumnoIdAndAnioEscolarIdAndTipo(UUID alumnoId, UUID anioEscolarId, String tipo);

    Optional<Beca> findByAlumnoIdAndAnioEscolarIdAndTipo(UUID alumnoId, UUID anioEscolarId, String tipo);

    @Query("SELECT COALESCE(SUM(b.porcentaje), 0) FROM Beca b " +
           "WHERE b.alumno.id = :alumnoId " +
           "AND b.anioEscolar.id = :anioEscolarId " +
           "AND b.vigente = true")
    BigDecimal sumPorcentajeVigenteByAlumnoAndAnioEscolar(
            @Param("alumnoId") UUID alumnoId,
            @Param("anioEscolarId") UUID anioEscolarId);
}
