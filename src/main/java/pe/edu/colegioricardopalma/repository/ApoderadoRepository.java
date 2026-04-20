package pe.edu.colegioricardopalma.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.Apoderado;
import pe.edu.colegioricardopalma.entity.Estado;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApoderadoRepository extends JpaRepository<Apoderado, UUID> {

    Optional<Apoderado> findByDni(String dni);

    Optional<Apoderado> findByEmail(String email);

    Optional<Apoderado> findByUsuarioId(UUID usuarioId);

    @Query("SELECT a FROM Apoderado a LEFT JOIN FETCH a.usuario")
    List<Apoderado> findActivos();

    @Query("SELECT a FROM Apoderado a")
    Page<Apoderado> findActivos(Pageable pageable);

    @Query("SELECT a FROM Apoderado a WHERE " +
           "(LOWER(a.nombres) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(a.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(a.dni) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(a.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Apoderado> searchApoderados(
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT a FROM Apoderado a LEFT JOIN FETCH a.usuario LEFT JOIN FETCH a.alumnos al LEFT JOIN FETCH al.alumno WHERE a.id = :id")
    Optional<Apoderado> findByIdWithDetails(@Param("id") UUID id);

    boolean existsByDni(String dni);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(a) FROM Apoderado a WHERE a.estado = 'ACTIVO'")
    Long countActivos();
}
