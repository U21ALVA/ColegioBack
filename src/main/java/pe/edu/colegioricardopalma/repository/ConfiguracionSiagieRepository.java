package pe.edu.colegioricardopalma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.ConfiguracionSiagie;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfiguracionSiagieRepository extends JpaRepository<ConfiguracionSiagie, UUID> {

    Optional<ConfiguracionSiagie> findTopByOrderByUpdatedAtDesc();
}
