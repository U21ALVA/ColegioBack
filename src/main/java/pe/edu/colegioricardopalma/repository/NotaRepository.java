package pe.edu.colegioricardopalma.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.Nota;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotaRepository extends JpaRepository<Nota, UUID> {

    // Find by alumno
    List<Nota> findByAlumnoId(UUID alumnoId);

    @Query("SELECT n FROM Nota n " +
           "JOIN FETCH n.curso c " +
           "JOIN FETCH n.bimestre b " +
           "JOIN FETCH b.anioEscolar " +
           "WHERE n.alumno.id = :alumnoId " +
           "ORDER BY b.numero ASC, c.nombre ASC")
    List<Nota> findByAlumnoIdWithDetails(@Param("alumnoId") UUID alumnoId);

    // Find by curso
    List<Nota> findByCursoId(UUID cursoId);

    // Find by bimestre
    List<Nota> findByBimestreId(UUID bimestreId);

    // Find by curso and bimestre
    @Query("SELECT n FROM Nota n " +
           "JOIN FETCH n.alumno a " +
           "LEFT JOIN FETCH n.docente d " +
           "WHERE n.curso.id = :cursoId AND n.bimestre.id = :bimestreId " +
           "ORDER BY a.apellidos ASC, a.nombres ASC")
    List<Nota> findByCursoIdAndBimestreId(
            @Param("cursoId") UUID cursoId,
            @Param("bimestreId") UUID bimestreId);

    // Find by alumno and bimestre
    @Query("SELECT n FROM Nota n " +
           "JOIN FETCH n.curso c " +
           "WHERE n.alumno.id = :alumnoId AND n.bimestre.id = :bimestreId " +
           "ORDER BY c.nombre ASC")
    List<Nota> findByAlumnoIdAndBimestreId(
            @Param("alumnoId") UUID alumnoId,
            @Param("bimestreId") UUID bimestreId);

    // Find by alumno, curso and bimestre (unique)
    Optional<Nota> findByAlumnoIdAndCursoIdAndBimestreId(UUID alumnoId, UUID cursoId, UUID bimestreId);

    // Find by docente
    List<Nota> findByDocenteId(UUID docenteId);

    // Find by docente and bimestre
    @Query("SELECT n FROM Nota n " +
           "JOIN FETCH n.alumno a " +
           "JOIN FETCH n.curso c " +
           "WHERE n.docente.id = :docenteId AND n.bimestre.id = :bimestreId " +
           "ORDER BY c.nombre ASC, a.apellidos ASC")
    List<Nota> findByDocenteIdAndBimestreId(
            @Param("docenteId") UUID docenteId,
            @Param("bimestreId") UUID bimestreId);

    // Find with full details
    @Query("SELECT n FROM Nota n " +
           "JOIN FETCH n.alumno a " +
           "JOIN FETCH n.curso c " +
           "JOIN FETCH n.bimestre b " +
           "JOIN FETCH b.anioEscolar " +
           "LEFT JOIN FETCH n.docente d " +
           "WHERE n.id = :id")
    Optional<Nota> findByIdWithDetails(@Param("id") UUID id);

    // Find all grades for a student in a school year
    @Query("SELECT n FROM Nota n " +
           "JOIN FETCH n.curso c " +
           "JOIN FETCH n.bimestre b " +
           "WHERE n.alumno.id = :alumnoId " +
           "AND b.anioEscolar.id = :anioEscolarId " +
           "ORDER BY c.nombre ASC, b.numero ASC")
    List<Nota> findByAlumnoIdAndAnioEscolarId(
            @Param("alumnoId") UUID alumnoId,
            @Param("anioEscolarId") UUID anioEscolarId);

    // Find grades for multiple students (for bulk operations)
    @Query("SELECT n FROM Nota n " +
           "WHERE n.curso.id = :cursoId " +
           "AND n.bimestre.id = :bimestreId " +
           "AND n.alumno.id IN :alumnoIds")
    List<Nota> findByCursoIdAndBimestreIdAndAlumnoIds(
            @Param("cursoId") UUID cursoId,
            @Param("bimestreId") UUID bimestreId,
            @Param("alumnoIds") List<UUID> alumnoIds);

    @Query("SELECT n FROM Nota n " +
           "JOIN FETCH n.alumno a " +
           "WHERE n.curso.id = :cursoId " +
           "AND n.bimestre.id = :bimestreId " +
           "AND a.id IN :alumnoIds")
    List<Nota> findByCursoBimestreAndAlumnoIdsWithAlumno(
            @Param("cursoId") UUID cursoId,
            @Param("bimestreId") UUID bimestreId,
            @Param("alumnoIds") List<UUID> alumnoIds);

    // Paginated search with filters
    @Query("SELECT n FROM Nota n " +
           "JOIN n.alumno a " +
           "JOIN n.curso c " +
           "JOIN n.bimestre b " +
           "WHERE (:alumnoId IS NULL OR n.alumno.id = :alumnoId) " +
           "AND (:cursoId IS NULL OR n.curso.id = :cursoId) " +
           "AND (:bimestreId IS NULL OR n.bimestre.id = :bimestreId) " +
           "AND (:docenteId IS NULL OR n.docente.id = :docenteId)")
    Page<Nota> findWithFilters(
            @Param("alumnoId") UUID alumnoId,
            @Param("cursoId") UUID cursoId,
            @Param("bimestreId") UUID bimestreId,
            @Param("docenteId") UUID docenteId,
            Pageable pageable);

    // Check if grade exists
    boolean existsByAlumnoIdAndCursoIdAndBimestreId(UUID alumnoId, UUID cursoId, UUID bimestreId);

    // Count grades by literal for statistics
    @Query("SELECT n.literal, COUNT(n) FROM Nota n " +
           "WHERE n.curso.id = :cursoId AND n.bimestre.id = :bimestreId " +
           "AND n.literal IS NOT NULL " +
           "GROUP BY n.literal")
    List<Object[]> countByLiteralForCursoAndBimestre(
            @Param("cursoId") UUID cursoId,
            @Param("bimestreId") UUID bimestreId);

    // Find students needing recovery (nota_final < 11)
    @Query("SELECT n FROM Nota n " +
           "JOIN FETCH n.alumno a " +
           "JOIN FETCH n.curso c " +
           "WHERE n.bimestre.id = :bimestreId " +
           "AND n.notaFinal < 11 " +
           "ORDER BY a.apellidos ASC, a.nombres ASC")
    List<Nota> findStudentsNeedingRecovery(@Param("bimestreId") UUID bimestreId);
}
