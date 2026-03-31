package pe.edu.colegioricardopalma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.AnioEscolar;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnioEscolarRepository extends JpaRepository<AnioEscolar, UUID> {

    Optional<AnioEscolar> findByAnio(Integer anio);

    Optional<AnioEscolar> findByActivoTrue();

    @Query("SELECT a FROM AnioEscolar a ORDER BY a.anio DESC")
    List<AnioEscolar> findAllOrderByAnioDesc();

    boolean existsByAnio(Integer anio);
}
