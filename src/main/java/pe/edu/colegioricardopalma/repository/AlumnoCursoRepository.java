package pe.edu.colegioricardopalma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.AlumnoCurso;
import pe.edu.colegioricardopalma.entity.Estado;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlumnoCursoRepository extends JpaRepository<AlumnoCurso, UUID> {

    @Query("SELECT ac FROM AlumnoCurso ac " +
            "JOIN FETCH ac.alumno a " +
            "JOIN FETCH ac.curso c " +
            "JOIN FETCH ac.seccion s " +
            "JOIN FETCH ac.anioEscolar ae " +
            "WHERE (:anioEscolarId IS NULL OR ae.id = :anioEscolarId) " +
            "AND (:seccionId IS NULL OR s.id = :seccionId) " +
            "AND (:cursoId IS NULL OR c.id = :cursoId) " +
            "AND (:alumnoId IS NULL OR a.id = :alumnoId) " +
            "AND ac.estado = 'ACTIVO' " +
            "ORDER BY a.apellidos, a.nombres, c.nombre")
    List<AlumnoCurso> findWithFilters(
            @Param("anioEscolarId") UUID anioEscolarId,
            @Param("seccionId") UUID seccionId,
            @Param("cursoId") UUID cursoId,
            @Param("alumnoId") UUID alumnoId
    );

    @Query("SELECT ac FROM AlumnoCurso ac " +
            "JOIN FETCH ac.alumno a " +
            "WHERE ac.curso.id = :cursoId " +
            "AND ac.seccion.id = :seccionId " +
            "AND ac.anioEscolar.id = :anioEscolarId " +
            "AND ac.estado = 'ACTIVO' " +
            "ORDER BY a.apellidos, a.nombres")
    List<AlumnoCurso> findActivosByCursoSeccionAnio(
            @Param("cursoId") UUID cursoId,
            @Param("seccionId") UUID seccionId,
            @Param("anioEscolarId") UUID anioEscolarId
    );

    boolean existsByAlumnoIdAndCursoIdAndSeccionIdAndAnioEscolarIdAndEstado(
            UUID alumnoId,
            UUID cursoId,
            UUID seccionId,
            UUID anioEscolarId,
            Estado estado
    );

    Optional<AlumnoCurso> findByAlumnoIdAndCursoIdAndSeccionIdAndAnioEscolarId(
            UUID alumnoId,
            UUID cursoId,
            UUID seccionId,
            UUID anioEscolarId
    );
}
