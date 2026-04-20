package pe.edu.colegioricardopalma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.colegioricardopalma.entity.AlumnoApoderado;
import pe.edu.colegioricardopalma.entity.Apoderado;
import pe.edu.colegioricardopalma.entity.Nivel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlumnoApoderadoRepository extends JpaRepository<AlumnoApoderado, UUID> {

    List<AlumnoApoderado> findByAlumnoId(UUID alumnoId);

    List<AlumnoApoderado> findByApoderadoId(UUID apoderadoId);

    Optional<AlumnoApoderado> findByAlumnoIdAndApoderadoId(UUID alumnoId, UUID apoderadoId);

    @Query("SELECT aa FROM AlumnoApoderado aa JOIN FETCH aa.apoderado WHERE aa.alumno.id = :alumnoId")
    List<AlumnoApoderado> findByAlumnoIdWithApoderado(@Param("alumnoId") UUID alumnoId);

    @Query("SELECT aa FROM AlumnoApoderado aa " +
            "JOIN FETCH aa.alumno a " +
            "JOIN FETCH aa.apoderado ap " +
            "LEFT JOIN FETCH a.grado " +
            "LEFT JOIN FETCH a.seccion " +
            "WHERE aa.apoderado.id = :apoderadoId")
    List<AlumnoApoderado> findByApoderadoIdWithAlumno(@Param("apoderadoId") UUID apoderadoId);

    @Query("SELECT DISTINCT aa.apoderado FROM AlumnoApoderado aa " +
            "JOIN aa.alumno al " +
            "WHERE al.grado.id IN :gradoIds")
    List<Apoderado> findDistinctApoderadosByGradoIds(@Param("gradoIds") List<UUID> gradoIds);

    @Query("SELECT DISTINCT aa.apoderado FROM AlumnoApoderado aa " +
            "JOIN aa.alumno al " +
            "WHERE al.seccion.id IN :seccionIds")
    List<Apoderado> findDistinctApoderadosBySeccionIds(@Param("seccionIds") List<UUID> seccionIds);

    @Query("SELECT DISTINCT aa.apoderado FROM AlumnoApoderado aa " +
            "JOIN aa.alumno al " +
            "JOIN al.grado g " +
            "WHERE g.nivel = :nivel")
    List<Apoderado> findDistinctApoderadosByNivel(@Param("nivel") Nivel nivel);

    @Query("SELECT aa FROM AlumnoApoderado aa WHERE aa.alumno.id = :alumnoId AND aa.esPrincipal = true")
    Optional<AlumnoApoderado> findApoderadoPrincipal(@Param("alumnoId") UUID alumnoId);

    boolean existsByAlumnoIdAndApoderadoId(UUID alumnoId, UUID apoderadoId);

    void deleteByAlumnoIdAndApoderadoId(UUID alumnoId, UUID apoderadoId);

    long countByApoderadoId(UUID apoderadoId);
}
