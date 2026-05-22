package pe.edu.colegioricardopalma.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.NotaHistorial;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotaHistorialRepository extends JpaRepository<NotaHistorial, UUID> {

    // Find by nota
    List<NotaHistorial> findByNotaIdOrderByCreatedAtDesc(UUID notaId);

    // Find by usuario
    List<NotaHistorial> findByUsuarioIdOrderByCreatedAtDesc(UUID usuarioId);

    // Find with details
    @Query("SELECT nh FROM NotaHistorial nh " +
           "JOIN FETCH nh.nota n " +
           "JOIN FETCH n.alumno a " +
           "JOIN FETCH n.curso c " +
           "JOIN FETCH n.bimestre b " +
           "LEFT JOIN FETCH n.docente d " +
           "LEFT JOIN FETCH nh.usuario u " +
           "WHERE nh.nota.id = :notaId " +
           "ORDER BY nh.createdAt DESC")
    List<NotaHistorial> findByNotaIdWithDetails(@Param("notaId") UUID notaId);

    // Paginated search with filters
    @Query(value = "SELECT nh FROM NotaHistorial nh " +
           "JOIN FETCH nh.nota n " +
           "JOIN FETCH n.alumno a " +
           "JOIN FETCH n.curso c " +
           "JOIN FETCH n.bimestre b " +
           "LEFT JOIN FETCH n.docente d " +
           "LEFT JOIN FETCH nh.usuario u " +
           "WHERE (:notaId IS NULL OR nh.nota.id = :notaId) " +
           "AND (:usuarioId IS NULL OR nh.usuario.id = :usuarioId) " +
           "AND (:cursoId IS NULL OR n.curso.id = :cursoId) " +
           "AND (:docenteId IS NULL OR n.docente.id = :docenteId) " +
           "AND (:desde IS NULL OR nh.createdAt >= :desde) " +
           "AND (:hasta IS NULL OR nh.createdAt <= :hasta)",
           countQuery = "SELECT COUNT(nh) FROM NotaHistorial nh " +
                   "JOIN nh.nota n " +
                   "WHERE (:notaId IS NULL OR nh.nota.id = :notaId) " +
                   "AND (:usuarioId IS NULL OR nh.usuario.id = :usuarioId) " +
                   "AND (:cursoId IS NULL OR n.curso.id = :cursoId) " +
                   "AND (:docenteId IS NULL OR n.docente.id = :docenteId) " +
                   "AND (:desde IS NULL OR nh.createdAt >= :desde) " +
                   "AND (:hasta IS NULL OR nh.createdAt <= :hasta)")
    Page<NotaHistorial> findWithFilters(
            @Param("notaId") UUID notaId,
            @Param("usuarioId") UUID usuarioId,
            @Param("cursoId") UUID cursoId,
            @Param("docenteId") UUID docenteId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable);

    // Find recent changes for a course
    @Query("SELECT nh FROM NotaHistorial nh " +
           "JOIN FETCH nh.nota n " +
           "JOIN FETCH n.alumno a " +
           "JOIN FETCH n.curso c " +
           "JOIN FETCH n.bimestre b " +
           "LEFT JOIN FETCH n.docente d " +
           "LEFT JOIN FETCH nh.usuario u " +
           "WHERE n.curso.id = :cursoId " +
           "ORDER BY nh.createdAt DESC")
    List<NotaHistorial> findByCursoIdOrderByCreatedAtDesc(@Param("cursoId") UUID cursoId);

    // Find changes by date range
    @Query("SELECT nh FROM NotaHistorial nh " +
           "JOIN FETCH nh.nota n " +
           "JOIN FETCH n.alumno a " +
           "JOIN FETCH n.curso c " +
           "JOIN FETCH n.bimestre b " +
           "LEFT JOIN FETCH n.docente d " +
           "LEFT JOIN FETCH nh.usuario u " +
           "WHERE nh.createdAt BETWEEN :desde AND :hasta " +
           "ORDER BY nh.createdAt DESC")
    List<NotaHistorial> findByDateRange(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    @Query("SELECT nh FROM NotaHistorial nh " +
           "JOIN FETCH nh.nota n " +
           "JOIN FETCH n.alumno a " +
           "JOIN FETCH n.curso c " +
           "JOIN FETCH n.bimestre b " +
           "LEFT JOIN FETCH n.docente d " +
           "LEFT JOIN FETCH nh.usuario u " +
           "WHERE n.alumno.id = :alumnoId " +
           "ORDER BY nh.createdAt DESC")
    List<NotaHistorial> findByAlumnoIdWithDetails(@Param("alumnoId") UUID alumnoId);

    // Count changes by docente
    @Query("SELECT COUNT(nh) FROM NotaHistorial nh " +
           "JOIN nh.nota n " +
           "WHERE n.docente.id = :docenteId")
    Long countChangesByDocente(@Param("docenteId") UUID docenteId);
}
