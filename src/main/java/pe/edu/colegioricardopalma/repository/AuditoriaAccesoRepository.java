package pe.edu.colegioricardopalma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.AuditoriaAcceso;

import java.util.UUID;

@Repository
public interface AuditoriaAccesoRepository extends JpaRepository<AuditoriaAcceso, UUID> {
}
