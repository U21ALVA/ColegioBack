package pe.edu.colegioricardopalma.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.Docente;
import pe.edu.colegioricardopalma.entity.Estado;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, UUID> {

    Optional<Docente> findByDni(String dni);

    Optional<Docente> findByEmail(String email);

    Optional<Docente> findByUsuarioId(UUID usuarioId);

    @Query("SELECT d FROM Docente d LEFT JOIN FETCH d.usuario")
    List<Docente> findActivos();

    @Query("SELECT d FROM Docente d")
    Page<Docente> findActivos(Pageable pageable);

    @Query("SELECT d FROM Docente d WHERE " +
           "(LOWER(d.nombres) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.dni) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Docente> searchDocentes(
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT d FROM Docente d LEFT JOIN FETCH d.usuario WHERE d.id = :id")
    Optional<Docente> findByIdWithUsuario(@Param("id") UUID id);

    boolean existsByDni(String dni);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(d) FROM Docente d WHERE d.estado = 'ACTIVO'")
    Long countActivos();
}
