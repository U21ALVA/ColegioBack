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

    List<Grado> findByEstadoOrderByOrdenAsc(Estado estado);

    @Query("SELECT g FROM Grado g WHERE g.estado = :estado ORDER BY g.orden ASC")
    List<Grado> findAllActiveOrderByOrden(@Param("estado") Estado estado);

    Optional<Grado> findByNombreAndNivel(String nombre, Nivel nivel);

    @Query("SELECT g FROM Grado g LEFT JOIN FETCH g.secciones WHERE g.id = :id")
    Optional<Grado> findByIdWithSecciones(@Param("id") UUID id);

    Page<Grado> findByEstado(Estado estado, Pageable pageable);

    @Query("SELECT g FROM Grado g ORDER BY g.orden ASC")
    List<Grado> findAllOrderByOrden();

    boolean existsByNombreAndNivel(String nombre, Nivel nivel);
}
