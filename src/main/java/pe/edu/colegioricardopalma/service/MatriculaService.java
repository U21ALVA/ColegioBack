package pe.edu.colegioricardopalma.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.AlumnoDto;
import pe.edu.colegioricardopalma.dto.MatriculaCreateRequest;
import pe.edu.colegioricardopalma.dto.MatriculaDto;
import pe.edu.colegioricardopalma.entity.*;
import pe.edu.colegioricardopalma.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatriculaService {

    private final AlumnoCursoRepository alumnoCursoRepository;
    private final AlumnoRepository alumnoRepository;
    private final CursoRepository cursoRepository;
    private final SeccionRepository seccionRepository;
    private final AnioEscolarRepository anioEscolarRepository;

    @Transactional(readOnly = true)
    public List<MatriculaDto> findWithFilters(UUID anioEscolarId, UUID seccionId, UUID cursoId, UUID alumnoId) {
        return alumnoCursoRepository.findWithFilters(anioEscolarId, seccionId, cursoId, alumnoId)
                .stream()
                .map(MatriculaDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public MatriculaDto create(MatriculaCreateRequest request) {
        Alumno alumno = alumnoRepository.findById(request.getAlumnoId())
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado"));
        Curso curso = cursoRepository.findById(request.getCursoId())
                .orElseThrow(() -> new EntityNotFoundException("Curso no encontrado"));
        Seccion seccion = seccionRepository.findByIdWithGrado(request.getSeccionId())
                .orElseThrow(() -> new EntityNotFoundException("Sección no encontrada"));
        AnioEscolar anio = anioEscolarRepository.findById(request.getAnioEscolarId())
                .orElseThrow(() -> new EntityNotFoundException("Año escolar no encontrado"));

        if (alumno.getSeccion() == null || !alumno.getSeccion().getId().equals(seccion.getId())) {
            throw new IllegalArgumentException("El alumno no pertenece a la sección seleccionada");
        }

        if (alumno.getGrado() == null || seccion.getGrado() == null || alumno.getGrado().getNivel() != curso.getNivel()) {
            throw new IllegalArgumentException("Nivel de curso incompatible con el alumno");
        }

        boolean exists = alumnoCursoRepository.existsByAlumnoIdAndCursoIdAndSeccionIdAndAnioEscolarIdAndEstado(
                alumno.getId(), curso.getId(), seccion.getId(), anio.getId(), Estado.ACTIVO
        );
        if (exists) {
            throw new IllegalArgumentException("La matrícula ya existe");
        }

        AlumnoCurso ac = alumnoCursoRepository.findByAlumnoIdAndCursoIdAndSeccionIdAndAnioEscolarId(
                        alumno.getId(), curso.getId(), seccion.getId(), anio.getId())
                .orElse(AlumnoCurso.builder()
                        .alumno(alumno)
                        .curso(curso)
                        .seccion(seccion)
                        .anioEscolar(anio)
                        .build());

        ac.setEstado(Estado.ACTIVO);
        ac.setOrigen("MANUAL");

        return MatriculaDto.fromEntity(alumnoCursoRepository.save(ac));
    }

    @Transactional
    public List<MatriculaDto> generarPorSeccion(UUID seccionId, UUID anioEscolarId) {
        Seccion seccion = seccionRepository.findByIdWithGrado(seccionId)
                .orElseThrow(() -> new EntityNotFoundException("Sección no encontrada"));
        AnioEscolar anio = anioEscolarRepository.findById(anioEscolarId)
                .orElseThrow(() -> new EntityNotFoundException("Año escolar no encontrado"));

        Nivel nivel = seccion.getGrado().getNivel();
        List<Curso> cursos = cursoRepository.findByNivelActivos(nivel);
        List<Alumno> alumnos = alumnoRepository.findByGradoAndSeccionActivos(seccion.getGrado().getId(), seccion.getId());

        List<MatriculaDto> created = new ArrayList<>();

        for (Alumno alumno : alumnos) {
            for (Curso curso : cursos) {
                boolean exists = alumnoCursoRepository.existsByAlumnoIdAndCursoIdAndSeccionIdAndAnioEscolarIdAndEstado(
                        alumno.getId(), curso.getId(), seccion.getId(), anio.getId(), Estado.ACTIVO
                );
                if (exists) continue;

                AlumnoCurso ac = alumnoCursoRepository.findByAlumnoIdAndCursoIdAndSeccionIdAndAnioEscolarId(
                                alumno.getId(), curso.getId(), seccion.getId(), anio.getId())
                        .orElse(AlumnoCurso.builder()
                                .alumno(alumno)
                                .curso(curso)
                                .seccion(seccion)
                                .anioEscolar(anio)
                                .build());

                ac.setEstado(Estado.ACTIVO);
                ac.setOrigen("AUTO");
                created.add(MatriculaDto.fromEntity(alumnoCursoRepository.save(ac)));
            }
        }

        return created;
    }

    @Transactional
    public void delete(UUID id) {
        AlumnoCurso ac = alumnoCursoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Matrícula no encontrada"));
        ac.setEstado(Estado.ELIMINADO);
        alumnoCursoRepository.save(ac);
    }

    @Transactional(readOnly = true)
    public List<AlumnoDto> findAlumnosMatriculados(UUID cursoId, UUID seccionId, UUID anioEscolarId) {
        return alumnoCursoRepository.findActivosByCursoSeccionAnio(cursoId, seccionId, anioEscolarId)
                .stream()
                .map(AlumnoCurso::getAlumno)
                .map(AlumnoDto::fromEntity)
                .collect(Collectors.toList());
    }
}
