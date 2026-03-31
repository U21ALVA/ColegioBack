package pe.edu.colegioricardopalma.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.Comunicado;
import pe.edu.colegioricardopalma.entity.ComunicadoEstado;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComunicadoRepository extends JpaRepository<Comunicado, UUID> {

    @Query("SELECT c FROM Comunicado c JOIN FETCH c.createdBy WHERE c.id = :id")
    Optional<Comunicado> findByIdWithCreator(@Param("id") UUID id);

    @Query(value = "SELECT c FROM Comunicado c JOIN FETCH c.createdBy ORDER BY c.createdAt DESC",
            countQuery = "SELECT COUNT(c) FROM Comunicado c")
    Page<Comunicado> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Comunicado> findByEstadoOrderByFechaPublicacionDesc(ComunicadoEstado estado, Pageable pageable);
}
