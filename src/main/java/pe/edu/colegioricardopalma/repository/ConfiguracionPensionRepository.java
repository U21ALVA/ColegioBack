package pe.edu.colegioricardopalma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.ConfiguracionPension;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfiguracionPensionRepository extends JpaRepository<ConfiguracionPension, UUID> {

    @Query("SELECT cp FROM ConfiguracionPension cp WHERE cp.anioEscolar.id = :anioEscolarId")
    Optional<ConfiguracionPension> findByAnioEscolarId(@Param("anioEscolarId") UUID anioEscolarId);

    @Query("SELECT cp FROM ConfiguracionPension cp JOIN FETCH cp.anioEscolar WHERE cp.anioEscolar.activo = true")
    Optional<ConfiguracionPension> findByAnioEscolarActivo();

    @Query("SELECT cp FROM ConfiguracionPension cp JOIN FETCH cp.anioEscolar ae WHERE ae.id = :anioEscolarId")
    Optional<ConfiguracionPension> findByAnioEscolarIdWithDetails(@Param("anioEscolarId") UUID anioEscolarId);

    boolean existsByAnioEscolarId(UUID anioEscolarId);
}
