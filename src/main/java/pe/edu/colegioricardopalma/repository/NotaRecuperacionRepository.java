package pe.edu.colegioricardopalma.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.NotaRecuperacion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotaRecuperacionRepository extends JpaRepository<NotaRecuperacion, UUID> {

    // Find by alumno
    List<NotaRecuperacion> findByAlumnoId(UUID alumnoId);

    // Find by curso
    List<NotaRecuperacion> findByCursoId(UUID cursoId);

    // Find by anio escolar
    List<NotaRecuperacion> findByAnioEscolarId(UUID anioEscolarId);

    // Find unique recovery grade
    Optional<NotaRecuperacion> findByAlumnoIdAndCursoIdAndAnioEscolarId(
            UUID alumnoId, UUID cursoId, UUID anioEscolarId);

    // Find with details
    @Query("SELECT nr FROM NotaRecuperacion nr " +
           "JOIN FETCH nr.alumno a " +
           "JOIN FETCH nr.curso c " +
           "JOIN FETCH nr.anioEscolar ae " +
           "LEFT JOIN FETCH nr.docente d " +
           "WHERE nr.id = :id")
    Optional<NotaRecuperacion> findByIdWithDetails(@Param("id") UUID id);

    // Find all for a student in a year
    @Query("SELECT nr FROM NotaRecuperacion nr " +
           "JOIN FETCH nr.curso c " +
           "WHERE nr.alumno.id = :alumnoId " +
           "AND nr.anioEscolar.id = :anioEscolarId " +
           "ORDER BY c.nombre ASC")
    List<NotaRecuperacion> findByAlumnoIdAndAnioEscolarIdWithDetails(
            @Param("alumnoId") UUID alumnoId,
            @Param("anioEscolarId") UUID anioEscolarId);

    // Find by docente
    List<NotaRecuperacion> findByDocenteId(UUID docenteId);

    // Paginated search
    @Query("SELECT nr FROM NotaRecuperacion nr " +
           "JOIN nr.alumno a " +
           "JOIN nr.curso c " +
           "WHERE (:alumnoId IS NULL OR nr.alumno.id = :alumnoId) " +
           "AND (:cursoId IS NULL OR nr.curso.id = :cursoId) " +
           "AND (:anioEscolarId IS NULL OR nr.anioEscolar.id = :anioEscolarId) " +
           "AND (:aprobado IS NULL OR nr.aprobado = :aprobado)")
    Page<NotaRecuperacion> findWithFilters(
            @Param("alumnoId") UUID alumnoId,
            @Param("cursoId") UUID cursoId,
            @Param("anioEscolarId") UUID anioEscolarId,
            @Param("aprobado") Boolean aprobado,
            Pageable pageable);

    // Check if recovery exists
    boolean existsByAlumnoIdAndCursoIdAndAnioEscolarId(UUID alumnoId, UUID cursoId, UUID anioEscolarId);

    // Count passed/failed
    @Query("SELECT COUNT(nr) FROM NotaRecuperacion nr " +
           "WHERE nr.anioEscolar.id = :anioEscolarId AND nr.aprobado = :aprobado")
    Long countByAnioEscolarIdAndAprobado(
            @Param("anioEscolarId") UUID anioEscolarId,
            @Param("aprobado") Boolean aprobado);
}
