package pe.edu.colegioricardopalma.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.ComunicadoEntrega;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComunicadoEntregaRepository extends JpaRepository<ComunicadoEntrega, UUID> {

    @Query("SELECT ce FROM ComunicadoEntrega ce " +
            "JOIN FETCH ce.comunicado c " +
            "WHERE ce.apoderado.id = :apoderadoId " +
            "ORDER BY c.fechaPublicacion DESC, c.createdAt DESC")
    Page<ComunicadoEntrega> findByApoderadoIdOrderByComunicadoFecha(@Param("apoderadoId") UUID apoderadoId, Pageable pageable);

    @Query("SELECT ce FROM ComunicadoEntrega ce " +
            "JOIN FETCH ce.apoderado a " +
            "WHERE ce.comunicado.id = :comunicadoId")
    List<ComunicadoEntrega> findByComunicadoIdWithApoderado(@Param("comunicadoId") UUID comunicadoId);

    boolean existsByComunicadoIdAndApoderadoId(UUID comunicadoId, UUID apoderadoId);
}
