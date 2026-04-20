package pe.edu.colegioricardopalma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.Estado;
import pe.edu.colegioricardopalma.entity.Grado;
import pe.edu.colegioricardopalma.entity.Seccion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeccionRepository extends JpaRepository<Seccion, UUID> {

    List<Seccion> findByGradoIdOrderByNombreAsc(UUID gradoId);

    @Query("SELECT s FROM Seccion s WHERE s.grado.id = :gradoId ORDER BY s.nombre ASC")
    List<Seccion> findByGradoIdActivas(@Param("gradoId") UUID gradoId);

    Optional<Seccion> findByNombreAndGrado(String nombre, Grado grado);

    @Query("SELECT s FROM Seccion s JOIN FETCH s.grado WHERE s.id = :id")
    Optional<Seccion> findByIdWithGrado(@Param("id") UUID id);

    @Query("SELECT s FROM Seccion s JOIN FETCH s.grado g ORDER BY g.orden ASC, s.nombre ASC")
    List<Seccion> findAllActiveWithGrado();

    @Query("SELECT COUNT(a) FROM Alumno a WHERE a.seccion.id = :seccionId")
    Long countAlumnosActivos(@Param("seccionId") UUID seccionId);

    boolean existsByNombreAndGradoId(String nombre, UUID gradoId);
}
