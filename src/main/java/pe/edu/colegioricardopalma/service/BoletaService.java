package pe.edu.colegioricardopalma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.colegioricardopalma.dto.BoletaDto;
import pe.edu.colegioricardopalma.dto.NotaRecuperacionDto;
import pe.edu.colegioricardopalma.dto.ResumenAcademicoDto;
import pe.edu.colegioricardopalma.entity.*;
import pe.edu.colegioricardopalma.repository.*;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoletaService {

    private final NotaRepository notaRepository;
    private final NotaRecuperacionRepository notaRecuperacionRepository;
    private final AlumnoRepository alumnoRepository;
    private final BimestreRepository bimestreRepository;
    private final AnioEscolarRepository anioEscolarRepository;
    private final AlumnoApoderadoRepository alumnoApoderadoRepository;
    private final ApoderadoRepository apoderadoRepository;

    /**
     * Generate full report card (boleta) for a student in a school year.
     */
    public BoletaDto generateBoleta(UUID alumnoId, UUID anioEscolarId) {
        // Validate alumno
        Alumno alumno = alumnoRepository.findByIdWithGradoAndSeccion(alumnoId)
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado: " + alumnoId));

        // Validate anio escolar
        AnioEscolar anioEscolar = anioEscolarRepository.findById(anioEscolarId)
                .orElseThrow(() -> new EntityNotFoundException("Año escolar no encontrado: " + anioEscolarId));

        // Get all grades for the student in this year
        List<Nota> notas = notaRepository.findByAlumnoIdAndAnioEscolarId(alumnoId, anioEscolarId);

        // Get bimesters for this year
        List<Bimestre> bimestres = bimestreRepository.findByAnioEscolarIdOrderByNumeroAsc(anioEscolarId);

        // Group by course
        Map<UUID, List<Nota>> notasByCurso = notas.stream()
                .collect(Collectors.groupingBy(n -> n.getCurso().getId()));

        // Build course grades
        List<BoletaDto.CursoNotasDto> cursos = new ArrayList<>();
        BigDecimal sumaPromedios = BigDecimal.ZERO;
        int countCursos = 0;

        for (Map.Entry<UUID, List<Nota>> entry : notasByCurso.entrySet()) {
            List<Nota> notasCurso = entry.getValue();
            if (notasCurso.isEmpty()) continue;

            Curso curso = notasCurso.get(0).getCurso();

            // Build bimester grades
            List<BoletaDto.BimestreNotaDto> bimestreNotas = new ArrayList<>();
            BigDecimal sumaBimestres = BigDecimal.ZERO;
            int countBimestres = 0;

            for (Bimestre bimestre : bimestres) {
                Nota notaBimestre = notasCurso.stream()
                        .filter(n -> n.getBimestre().getId().equals(bimestre.getId()))
                        .findFirst()
                        .orElse(null);

                BoletaDto.BimestreNotaDto bimestreNota = BoletaDto.BimestreNotaDto.builder()
                        .bimestreId(bimestre.getId())
                        .numero(bimestre.getNumero())
                        .build();

                if (notaBimestre != null) {
                    bimestreNota.setN1(notaBimestre.getN1());
                    bimestreNota.setN2(notaBimestre.getN2());
                    bimestreNota.setN3(notaBimestre.getN3());
                    bimestreNota.setN4(notaBimestre.getN4());
                    bimestreNota.setNotaFinal(notaBimestre.getNotaFinal());
                    bimestreNota.setLiteral(notaBimestre.getLiteral());

                    if (notaBimestre.getNotaFinal() != null) {
                        sumaBimestres = sumaBimestres.add(notaBimestre.getNotaFinal());
                        countBimestres++;
                    }
                }

                bimestreNotas.add(bimestreNota);
            }

            // Calculate annual average for course
            BigDecimal promedioAnual = null;
            LiteralNota literalAnual = null;
            if (countBimestres > 0) {
                promedioAnual = sumaBimestres.divide(BigDecimal.valueOf(countBimestres), 2, RoundingMode.HALF_UP);
                literalAnual = LiteralNota.fromNota(promedioAnual.doubleValue());
                sumaPromedios = sumaPromedios.add(promedioAnual);
                countCursos++;
            }

            BoletaDto.CursoNotasDto cursoNotas = BoletaDto.CursoNotasDto.builder()
                    .cursoId(curso.getId())
                    .cursoNombre(curso.getNombre())
                    .bimestres(bimestreNotas)
                    .promedioAnual(promedioAnual)
                    .literalAnual(literalAnual)
                    .build();

            cursos.add(cursoNotas);
        }

        // Sort courses by name
        cursos.sort(Comparator.comparing(BoletaDto.CursoNotasDto::getCursoNombre));

        // Calculate general average
        BigDecimal promedioGeneral = null;
        LiteralNota literalGeneral = null;
        if (countCursos > 0) {
            promedioGeneral = sumaPromedios.divide(BigDecimal.valueOf(countCursos), 2, RoundingMode.HALF_UP);
            literalGeneral = LiteralNota.fromNota(promedioGeneral.doubleValue());
        }

        return BoletaDto.builder()
                .alumnoId(alumno.getId())
                .alumnoNombres(alumno.getNombres())
                .alumnoApellidos(alumno.getApellidos())
                .alumnoCodigo(alumno.getCodigoEstudiante())
                .gradoNombre(alumno.getGrado() != null ? alumno.getGrado().getNombre() : null)
                .seccionNombre(alumno.getSeccion() != null ? alumno.getSeccion().getNombre() : null)
                .anioEscolarId(anioEscolar.getId())
                .anioEscolar(anioEscolar.getAnio())
                .cursos(cursos)
                .promedioGeneral(promedioGeneral)
                .literalGeneral(literalGeneral)
                .build();
    }

    /**
     * Generate report card for a specific bimester.
     */
    public BoletaDto generateBoletaBimestre(UUID alumnoId, UUID bimestreId) {
        // Validate bimestre
        Bimestre bimestre = bimestreRepository.findByIdWithAnioEscolar(bimestreId)
                .orElseThrow(() -> new EntityNotFoundException("Bimestre no encontrado: " + bimestreId));

        // Generate full boleta and filter to single bimester
        BoletaDto boleta = generateBoleta(alumnoId, bimestre.getAnioEscolar().getId());

        // Filter to only include the requested bimester
        for (BoletaDto.CursoNotasDto curso : boleta.getCursos()) {
            List<BoletaDto.BimestreNotaDto> filteredBimestres = curso.getBimestres().stream()
                    .filter(b -> b.getBimestreId().equals(bimestreId))
                    .collect(Collectors.toList());
            curso.setBimestres(filteredBimestres);
        }

        return boleta;
    }

    /**
     * Generate academic summary for parent view.
     */
    public ResumenAcademicoDto generateResumenAcademico(UUID alumnoId) {
        // Validate alumno
        Alumno alumno = alumnoRepository.findByIdWithGradoAndSeccion(alumnoId)
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado: " + alumnoId));

        // Get active school year
        AnioEscolar anioEscolar = anioEscolarRepository.findByActivoTrue()
                .orElseThrow(() -> new EntityNotFoundException("No hay año escolar activo"));

        // Get open bimesters
        List<Bimestre> bimestresAbiertos = bimestreRepository.findBimestresAbiertosAnioActivo();
        Integer bimestreActual = bimestresAbiertos.isEmpty() ? null : 
                bimestresAbiertos.get(bimestresAbiertos.size() - 1).getNumero();

        // Get all grades for the year
        List<Nota> notas = notaRepository.findByAlumnoIdAndAnioEscolarId(alumnoId, anioEscolar.getId());

        // Group by course and calculate stats
        Map<UUID, List<Nota>> notasByCurso = notas.stream()
                .collect(Collectors.groupingBy(n -> n.getCurso().getId()));

        List<ResumenAcademicoDto.CursoResumenDto> cursos = new ArrayList<>();
        int cursosAprobados = 0;
        int cursosDesaprobados = 0;
        BigDecimal sumaNotas = BigDecimal.ZERO;
        int countNotas = 0;

        for (Map.Entry<UUID, List<Nota>> entry : notasByCurso.entrySet()) {
            List<Nota> notasCurso = entry.getValue();
            if (notasCurso.isEmpty()) continue;

            Curso curso = notasCurso.get(0).getCurso();

            // Get latest grade
            Nota ultimaNota = notasCurso.stream()
                    .filter(n -> n.getNotaFinal() != null)
                    .max(Comparator.comparing(n -> n.getBimestre().getNumero()))
                    .orElse(null);

            BigDecimal ultimaNotaValue = ultimaNota != null ? ultimaNota.getNotaFinal() : null;
            LiteralNota literal = ultimaNota != null ? ultimaNota.getLiteral() : null;

            // Calculate tendency
            String tendencia = calculateTendencia(notasCurso);

            cursos.add(ResumenAcademicoDto.CursoResumenDto.builder()
                    .cursoId(curso.getId())
                    .cursoNombre(curso.getNombre())
                    .ultimaNota(ultimaNotaValue)
                    .literal(literal)
                    .tendencia(tendencia)
                    .build());

            if (ultimaNotaValue != null) {
                sumaNotas = sumaNotas.add(ultimaNotaValue);
                countNotas++;
                if (ultimaNotaValue.compareTo(BigDecimal.valueOf(11)) >= 0) {
                    cursosAprobados++;
                } else {
                    cursosDesaprobados++;
                }
            }
        }

        // Sort by course name
        cursos.sort(Comparator.comparing(ResumenAcademicoDto.CursoResumenDto::getCursoNombre));

        // Calculate general average
        BigDecimal promedioGeneral = null;
        LiteralNota literalGeneral = null;
        if (countNotas > 0) {
            promedioGeneral = sumaNotas.divide(BigDecimal.valueOf(countNotas), 2, RoundingMode.HALF_UP);
            literalGeneral = LiteralNota.fromNota(promedioGeneral.doubleValue());
        }

        // Generate alerts
        List<ResumenAcademicoDto.AlertaDto> alertas = generateAlertas(cursos, alumnoId, anioEscolar.getId());

        return ResumenAcademicoDto.builder()
                .alumnoId(alumno.getId())
                .alumnoNombres(alumno.getNombres())
                .alumnoApellidos(alumno.getApellidos())
                .alumnoCodigo(alumno.getCodigoEstudiante())
                .gradoNombre(alumno.getGrado() != null ? alumno.getGrado().getNombre() : null)
                .seccionNombre(alumno.getSeccion() != null ? alumno.getSeccion().getNombre() : null)
                .anioEscolar(anioEscolar.getAnio())
                .bimestreActual(bimestreActual)
                .promedioGeneral(promedioGeneral)
                .literalGeneral(literalGeneral)
                .totalCursos(cursos.size())
                .cursosAprobados(cursosAprobados)
                .cursosDesaprobados(cursosDesaprobados)
                .cursos(cursos)
                .alertas(alertas)
                .build();
    }

    /**
     * Get children linked to a parent (apoderado).
     */
    public List<UUID> getHijosForApoderado(UUID apoderadoId) {
        return alumnoApoderadoRepository.findByApoderadoIdWithAlumno(apoderadoId).stream()
                .map(aa -> aa.getAlumno().getId())
                .collect(Collectors.toList());
    }

    /**
     * Check if parent has access to student.
     */
    public boolean isApoderadoOfAlumno(UUID apoderadoId, UUID alumnoId) {
        return alumnoApoderadoRepository.existsByAlumnoIdAndApoderadoId(alumnoId, apoderadoId);
    }

    /**
     * Get apoderado by usuario ID.
     */
    public UUID getApoderadoIdByUsuarioId(UUID usuarioId) {
        return apoderadoRepository.findByUsuarioId(usuarioId)
                .map(Apoderado::getId)
                .orElse(null);
    }

    private String calculateTendencia(List<Nota> notas) {
        List<Nota> notasOrdenadas = notas.stream()
                .filter(n -> n.getNotaFinal() != null)
                .sorted(Comparator.comparing(n -> n.getBimestre().getNumero()))
                .collect(Collectors.toList());

        if (notasOrdenadas.size() < 2) {
            return "ESTABLE";
        }

        BigDecimal primera = notasOrdenadas.get(0).getNotaFinal();
        BigDecimal ultima = notasOrdenadas.get(notasOrdenadas.size() - 1).getNotaFinal();

        int comparison = ultima.compareTo(primera);
        if (comparison > 0) {
            return "SUBIENDO";
        } else if (comparison < 0) {
            return "BAJANDO";
        } else {
            return "ESTABLE";
        }
    }

    private List<ResumenAcademicoDto.AlertaDto> generateAlertas(
            List<ResumenAcademicoDto.CursoResumenDto> cursos,
            UUID alumnoId,
            UUID anioEscolarId) {
        
        List<ResumenAcademicoDto.AlertaDto> alertas = new ArrayList<>();

        // Check for low performance (D grades)
        for (ResumenAcademicoDto.CursoResumenDto curso : cursos) {
            if (curso.getLiteral() == LiteralNota.D) {
                alertas.add(ResumenAcademicoDto.AlertaDto.builder()
                        .tipo("BAJO_RENDIMIENTO")
                        .mensaje("Necesita mejorar en " + curso.getCursoNombre())
                        .cursoNombre(curso.getCursoNombre())
                        .build());
            } else if (curso.getLiteral() == LiteralNota.A) {
                alertas.add(ResumenAcademicoDto.AlertaDto.builder()
                        .tipo("FELICITACION")
                        .mensaje("Excelente rendimiento en " + curso.getCursoNombre())
                        .cursoNombre(curso.getCursoNombre())
                        .build());
            }
        }

        // Check for pending recovery exams
        List<NotaRecuperacion> recuperaciones = notaRecuperacionRepository
                .findByAlumnoIdAndAnioEscolarIdWithDetails(alumnoId, anioEscolarId);
        
        for (NotaRecuperacion rec : recuperaciones) {
            if (!rec.getAprobado() && rec.getNotaRecuperacion() == null) {
                alertas.add(ResumenAcademicoDto.AlertaDto.builder()
                        .tipo("RECUPERACION_PENDIENTE")
                        .mensaje("Tiene examen de recuperación pendiente en " + rec.getCurso().getNombre())
                        .cursoNombre(rec.getCurso().getNombre())
                        .build());
            }
        }

        return alertas;
    }
}
