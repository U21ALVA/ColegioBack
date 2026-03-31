package pe.edu.colegioricardopalma.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.ExportacionSiagie;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExportacionSiagieRepository extends JpaRepository<ExportacionSiagie, UUID> {

    @Query("SELECT e FROM ExportacionSiagie e JOIN FETCH e.usuario ORDER BY COALESCE(e.fecha, e.createdAt) DESC")
    Page<ExportacionSiagie> findAllWithUsuario(Pageable pageable);

    @Query("SELECT e FROM ExportacionSiagie e JOIN FETCH e.usuario WHERE e.id = :id")
    Optional<ExportacionSiagie> findByIdWithUsuario(@Param("id") UUID id);
}
