package pe.edu.colegioricardopalma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.AuditoriaAcceso;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditoriaAccesoRepository extends JpaRepository<AuditoriaAcceso, UUID> {

    @Query("SELECT a FROM AuditoriaAcceso a LEFT JOIN FETCH a.usuario u ORDER BY a.createdAt DESC")
    List<AuditoriaAcceso> findRecent(Pageable pageable);
}
