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

    @Query("SELECT c FROM Curso c WHERE c.nivel = :nivel ORDER BY c.nombre ASC")
    List<Curso> findByNivelActivos(@Param("nivel") Nivel nivel);

    @Query("SELECT c FROM Curso c")
    Page<Curso> findActivos(Pageable pageable);

    @Query("SELECT c FROM Curso c ORDER BY c.nivel, c.nombre")
    List<Curso> findAllActiveOrderByNivelAndNombre();

    Optional<Curso> findByNombreAndNivel(String nombre, Nivel nivel);

    @Query("SELECT c FROM Curso c WHERE " +
           "(LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND c.nivel = :nivel")
    Page<Curso> searchCursosByNivel(
            @Param("search") String search,
            @Param("nivel") Nivel nivel,
            Pageable pageable);

    @Query("SELECT c FROM Curso c WHERE " +
           "(LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Curso> searchCursos(@Param("search") String search, Pageable pageable);

    boolean existsByNombreAndNivel(String nombre, Nivel nivel);
}
