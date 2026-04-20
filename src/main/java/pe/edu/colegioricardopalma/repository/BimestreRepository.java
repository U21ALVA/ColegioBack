package pe.edu.colegioricardopalma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.Bimestre;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BimestreRepository extends JpaRepository<Bimestre, UUID> {

    List<Bimestre> findByAnioEscolarIdOrderByNumeroAsc(UUID anioEscolarId);

    Optional<Bimestre> findByNumeroAndAnioEscolarId(Integer numero, UUID anioEscolarId);

    @Query("SELECT b FROM Bimestre b JOIN FETCH b.anioEscolar WHERE b.id = :id")
    Optional<Bimestre> findByIdWithAnioEscolar(@Param("id") UUID id);

    @Query("SELECT b FROM Bimestre b JOIN FETCH b.anioEscolar a WHERE a.activo = true ORDER BY b.numero ASC")
    List<Bimestre> findByAnioEscolarActivo();

    @Query("SELECT b FROM Bimestre b JOIN FETCH b.anioEscolar a WHERE a.activo = true AND b.cerrado = false ORDER BY b.numero ASC")
    List<Bimestre> findBimestresAbiertosAnioActivo();

    boolean existsByNumeroAndAnioEscolarId(Integer numero, UUID anioEscolarId);
}
