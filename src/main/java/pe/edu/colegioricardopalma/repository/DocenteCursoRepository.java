package pe.edu.colegioricardopalma.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.DocenteCurso;
import pe.edu.colegioricardopalma.entity.Estado;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocenteCursoRepository extends JpaRepository<DocenteCurso, UUID> {

    List<DocenteCurso> findByDocenteIdAndAnioEscolarId(UUID docenteId, UUID anioEscolarId);

    List<DocenteCurso> findByCursoIdAndAnioEscolarId(UUID cursoId, UUID anioEscolarId);

    List<DocenteCurso> findByGradoIdAndSeccionIdAndAnioEscolarId(UUID gradoId, UUID seccionId, UUID anioEscolarId);

    @Query("SELECT dc FROM DocenteCurso dc " +
           "JOIN FETCH dc.docente d " +
           "JOIN FETCH dc.curso c " +
           "JOIN FETCH dc.grado g " +
           "JOIN FETCH dc.seccion s " +
           "JOIN FETCH dc.anioEscolar a " +
           "WHERE dc.anioEscolar.id = :anioEscolarId " +
           "AND dc.estado = :estado " +
           "ORDER BY g.orden, s.nombre, c.nombre")
    List<DocenteCurso> findAllByAnioEscolarWithDetails(
            @Param("anioEscolarId") UUID anioEscolarId,
            @Param("estado") Estado estado);

    @Query("SELECT dc FROM DocenteCurso dc " +
           "JOIN FETCH dc.docente d " +
           "JOIN FETCH dc.curso c " +
           "JOIN FETCH dc.grado g " +
           "JOIN FETCH dc.seccion s " +
           "WHERE dc.docente.id = :docenteId " +
           "AND dc.anioEscolar.id = :anioEscolarId " +
           "AND dc.estado = :estado")
    List<DocenteCurso> findByDocenteAndAnioEscolarWithDetails(
            @Param("docenteId") UUID docenteId,
            @Param("anioEscolarId") UUID anioEscolarId,
            @Param("estado") Estado estado);

    @Query("SELECT dc FROM DocenteCurso dc WHERE " +
           "dc.docente.id = :docenteId AND " +
           "dc.curso.id = :cursoId AND " +
           "dc.grado.id = :gradoId AND " +
           "dc.seccion.id = :seccionId AND " +
           "dc.anioEscolar.id = :anioEscolarId")
    Optional<DocenteCurso> findExistingAssignment(
            @Param("docenteId") UUID docenteId,
            @Param("cursoId") UUID cursoId,
            @Param("gradoId") UUID gradoId,
            @Param("seccionId") UUID seccionId,
            @Param("anioEscolarId") UUID anioEscolarId);

    Page<DocenteCurso> findByAnioEscolarIdAndEstado(UUID anioEscolarId, Estado estado, Pageable pageable);

    boolean existsByDocenteIdAndCursoIdAndGradoIdAndSeccionIdAndAnioEscolarId(
            UUID docenteId, UUID cursoId, UUID gradoId, UUID seccionId, UUID anioEscolarId);
}
