package pe.edu.colegioricardopalma.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.Alumno;
import pe.edu.colegioricardopalma.entity.Estado;
import pe.edu.colegioricardopalma.entity.Nivel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlumnoRepository extends JpaRepository<Alumno, UUID> {

    Optional<Alumno> findByDni(String dni);

    Optional<Alumno> findByCodigoEstudiante(String codigoEstudiante);

    List<Alumno> findByGradoId(UUID gradoId);

    List<Alumno> findBySeccionId(UUID seccionId);

    @Query("SELECT a FROM Alumno a")
    Page<Alumno> findActivos(Pageable pageable);

    @Query("SELECT a FROM Alumno a " +
           "LEFT JOIN FETCH a.grado g " +
           "LEFT JOIN FETCH a.seccion s " +
           "ORDER BY a.apellidos ASC, a.nombres ASC")
    List<Alumno> findActivosWithDetails();

    @Query("SELECT a FROM Alumno a JOIN FETCH a.grado g LEFT JOIN FETCH a.seccion s WHERE a.grado.id = :gradoId AND a.seccion.id = :seccionId")
    List<Alumno> findByGradoAndSeccionActivos(
            @Param("gradoId") UUID gradoId,
            @Param("seccionId") UUID seccionId);

    @Query("SELECT a FROM Alumno a JOIN FETCH a.grado g LEFT JOIN FETCH a.seccion WHERE a.id = :id")
    Optional<Alumno> findByIdWithGradoAndSeccion(@Param("id") UUID id);

    @Query("SELECT a FROM Alumno a WHERE " +
           "(LOWER(a.nombres) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(a.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(a.dni) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(a.codigoEstudiante) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Alumno> searchAlumnos(
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT a FROM Alumno a WHERE " +
           "(LOWER(a.nombres) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(a.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(a.dni) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:gradoId IS NULL OR a.grado.id = :gradoId) " +
            "AND (:seccionId IS NULL OR a.seccion.id = :seccionId)")
    Page<Alumno> searchAlumnosWithFilters(
            @Param("search") String search,
            @Param("gradoId") UUID gradoId,
            @Param("seccionId") UUID seccionId,
            Pageable pageable);

    @Query("SELECT a FROM Alumno a JOIN a.grado g WHERE g.nivel = :nivel")
    List<Alumno> findByNivelActivos(@Param("nivel") Nivel nivel);

    @Query("SELECT a FROM Alumno a " +
           "LEFT JOIN FETCH a.grado g " +
           "LEFT JOIN FETCH a.seccion s " +
           "WHERE (:gradoId IS NULL OR g.id = :gradoId) " +
           "AND (:seccionId IS NULL OR s.id = :seccionId) " +
           "ORDER BY a.apellidos ASC, a.nombres ASC")
    List<Alumno> findActivosForSiagie(
            @Param("gradoId") UUID gradoId,
            @Param("seccionId") UUID seccionId);

    boolean existsByDni(String dni);

    boolean existsByCodigoEstudiante(String codigoEstudiante);

    @Query("SELECT COUNT(a) FROM Alumno a WHERE a.estado = 'ACTIVO'")
    Long countActivos();

    @Query("SELECT COUNT(a) FROM Alumno a WHERE a.grado.id = :gradoId AND a.estado = 'ACTIVO'")
    Long countActivosByGrado(@Param("gradoId") UUID gradoId);
}
