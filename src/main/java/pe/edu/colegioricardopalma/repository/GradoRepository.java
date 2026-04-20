package pe.edu.colegioricardopalma.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.Estado;
import pe.edu.colegioricardopalma.entity.Grado;
import pe.edu.colegioricardopalma.entity.Nivel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GradoRepository extends JpaRepository<Grado, UUID> {

    List<Grado> findByNivelOrderByOrdenAsc(Nivel nivel);

    @Query("SELECT g FROM Grado g ORDER BY g.orden ASC")
    List<Grado> findActivosOrderByOrdenAsc();

    @Query("SELECT g FROM Grado g ORDER BY g.orden ASC")
    List<Grado> findAllActiveOrderByOrden();

    Optional<Grado> findByNombreAndNivel(String nombre, Nivel nivel);

    @Query("SELECT g FROM Grado g LEFT JOIN FETCH g.secciones WHERE g.id = :id")
    Optional<Grado> findByIdWithSecciones(@Param("id") UUID id);

    @Query("SELECT g FROM Grado g")
    Page<Grado> findActivos(Pageable pageable);

    @Query("SELECT g FROM Grado g ORDER BY g.orden ASC")
    List<Grado> findAllOrderByOrden();

    boolean existsByNombreAndNivel(String nombre, Nivel nivel);
}
