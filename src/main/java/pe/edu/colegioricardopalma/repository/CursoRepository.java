package pe.edu.colegioricardopalma.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.Curso;
import pe.edu.colegioricardopalma.entity.Estado;
import pe.edu.colegioricardopalma.entity.Nivel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CursoRepository extends JpaRepository<Curso, UUID> {

    List<Curso> findByNivelOrderByNombreAsc(Nivel nivel);

    List<Curso> findByNivelAndEstado(Nivel nivel, Estado estado);

    Page<Curso> findByEstado(Estado estado, Pageable pageable);

    @Query("SELECT c FROM Curso c WHERE c.estado = :estado ORDER BY c.nivel, c.nombre")
    List<Curso> findAllActiveOrderByNivelAndNombre(@Param("estado") Estado estado);

    Optional<Curso> findByNombreAndNivel(String nombre, Nivel nivel);

    @Query("SELECT c FROM Curso c WHERE " +
           "(LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:nivel IS NULL OR c.nivel = :nivel) " +
           "AND c.estado = :estado")
    Page<Curso> searchCursos(
            @Param("search") String search,
            @Param("nivel") Nivel nivel,
            @Param("estado") Estado estado,
            Pageable pageable);

    boolean existsByNombreAndNivel(String nombre, Nivel nivel);
}
