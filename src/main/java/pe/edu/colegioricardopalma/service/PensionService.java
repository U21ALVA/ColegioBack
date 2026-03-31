package pe.edu.colegioricardopalma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.dto.PensionCreateRequest;
import pe.edu.colegioricardopalma.dto.PensionDto;
import pe.edu.colegioricardopalma.entity.*;
import pe.edu.colegioricardopalma.repository.*;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PensionService {

    private final PensionRepository pensionRepository;
    private final AlumnoRepository alumnoRepository;
    private final AnioEscolarRepository anioEscolarRepository;
    private final ConfiguracionPensionRepository configuracionRepository;
    private final BecaRepository becaRepository;

    // School months: March (3) to December (12)
    private static final int[] MESES_ESCOLARES = {3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

    public List<PensionDto> findAll() {
        return pensionRepository.findAll().stream()
                .map(PensionDto::fromEntity)
                .collect(Collectors.toList());
    }

    public PageResponse<PensionDto> findWithFilters(
            Integer mes, PensionEstado estado, UUID gradoId, Pageable pageable) {
        Page<PensionDto> page = pensionRepository.findActiveWithFilters(mes, estado, gradoId, pageable)
                .map(PensionDto::fromEntity);
        return PageResponse.from(page);
    }

    public PensionDto findById(UUID id) {
        Pension pension = pensionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Pensión no encontrada: " + id));
        return PensionDto.fromEntity(pension);
    }

    public List<PensionDto> findByAlumno(UUID alumnoId) {
        return pensionRepository.findByAlumnoId(alumnoId).stream()
                .map(PensionDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<PensionDto> findByAlumnoAndAnioEscolar(UUID alumnoId, UUID anioEscolarId) {
        return pensionRepository.findByAlumnoIdAndAnioEscolarId(alumnoId, anioEscolarId).stream()
                .map(PensionDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<PensionDto> findPendientesByAlumno(UUID alumnoId) {
        return pensionRepository.findByAlumnoIdAndEstado(alumnoId, PensionEstado.PENDIENTE).stream()
                .map(PensionDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<PensionDto> findPendientesByAlumnos(List<UUID> alumnoIds) {
        if (alumnoIds == null || alumnoIds.isEmpty()) {
            return List.of();
        }
        return pensionRepository.findPendientesByAlumnoIds(alumnoIds).stream()
                .map(PensionDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public PensionDto create(PensionCreateRequest request) {
        // Validate alumno
        Alumno alumno = alumnoRepository.findById(request.getAlumnoId())
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado: " + request.getAlumnoId()));

        // Validate año escolar
        AnioEscolar anioEscolar = anioEscolarRepository.findById(request.getAnioEscolarId())
                .orElseThrow(() -> new EntityNotFoundException("Año escolar no encontrado: " + request.getAnioEscolarId()));

        // Check for duplicate
        if (pensionRepository.existsByAlumnoIdAndAnioEscolarIdAndMes(
                request.getAlumnoId(), request.getAnioEscolarId(), request.getMes())) {
            throw new IllegalArgumentException("Ya existe una pensión para este alumno, año y mes");
        }

        Pension pension = Pension.builder()
                .alumno(alumno)
                .anioEscolar(anioEscolar)
                .mes(request.getMes())
                .monto(request.getMonto())
                .descuento(request.getDescuento() != null ? request.getDescuento() : BigDecimal.ZERO)
                .estado(PensionEstado.PENDIENTE)
                .build();

        pension.calcularMontoFinal();
        
        // Set due date
        pension.setFechaVencimiento(calcularFechaVencimiento(anioEscolar, request.getMes()));

        Pension saved = pensionRepository.save(pension);
        log.info("Pensión creada para alumno {} mes {}", alumno.getNombreCompleto(), request.getMes());

        return PensionDto.fromEntity(saved);
    }

    @Transactional
    public List<PensionDto> generarPensionesMensuales(UUID anioEscolarId, Integer mes) {
        // Get configuration for the year
        ConfiguracionPension config = configuracionRepository.findByAnioEscolarId(anioEscolarId)
                .orElseThrow(() -> new IllegalStateException("No hay configuración de pensión para este año escolar"));

        AnioEscolar anioEscolar = anioEscolarRepository.findById(anioEscolarId)
                .orElseThrow(() -> new EntityNotFoundException("Año escolar no encontrado"));

        // Get all active students
        List<Alumno> alumnos = alumnoRepository.findAll().stream()
                .filter(a -> a.getEstado() == Estado.ACTIVO)
                .collect(Collectors.toList());

        List<PensionDto> created = new ArrayList<>();
        int[] mesesAGenerar = mes != null ? new int[]{mes} : MESES_ESCOLARES;

        for (Alumno alumno : alumnos) {
            for (int m : mesesAGenerar) {
                // Skip if pension already exists
                if (pensionRepository.existsByAlumnoIdAndAnioEscolarIdAndMes(alumno.getId(), anioEscolarId, m)) {
                    continue;
                }

                // Calculate discount from active scholarships
                BigDecimal descuentoPorcentaje = becaRepository.sumPorcentajeVigenteByAlumnoAndAnioEscolar(
                        alumno.getId(), anioEscolarId);
                
                BigDecimal descuento = BigDecimal.ZERO;
                if (descuentoPorcentaje != null && descuentoPorcentaje.compareTo(BigDecimal.ZERO) > 0) {
                    // Cap at 100%
                    if (descuentoPorcentaje.compareTo(BigDecimal.valueOf(100)) > 0) {
                        descuentoPorcentaje = BigDecimal.valueOf(100);
                    }
                    descuento = config.getMontoBase()
                            .multiply(descuentoPorcentaje)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }

                Pension pension = Pension.builder()
                        .alumno(alumno)
                        .anioEscolar(anioEscolar)
                        .mes(m)
                        .monto(config.getMontoBase())
                        .descuento(descuento)
                        .estado(PensionEstado.PENDIENTE)
                        .fechaVencimiento(calcularFechaVencimiento(anioEscolar, m, config.getFechaVencimientoDia()))
                        .build();

                pension.calcularMontoFinal();
                Pension saved = pensionRepository.save(pension);
                created.add(PensionDto.fromEntity(saved));
            }
        }

        log.info("Generadas {} pensiones para año escolar {} mes {}", 
                created.size(), anioEscolar.getAnio(), mes != null ? mes : "todos");
        return created;
    }

    @Transactional
    public PensionDto updateEstado(UUID id, PensionEstado nuevoEstado) {
        Pension pension = pensionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Pensión no encontrada: " + id));

        pension.setEstado(nuevoEstado);
        Pension saved = pensionRepository.save(pension);
        log.info("Pensión {} actualizada a estado {}", id, nuevoEstado);

        return PensionDto.fromEntity(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Pension pension = pensionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pensión no encontrada: " + id));

        if (pension.getEstado() == PensionEstado.PAGADO) {
            throw new IllegalStateException("No se puede eliminar una pensión ya pagada");
        }

        pensionRepository.delete(pension);
        log.info("Pensión eliminada: {}", id);
    }

    private LocalDate calcularFechaVencimiento(AnioEscolar anioEscolar, int mes) {
        // Default: day 15 of the month
        return calcularFechaVencimiento(anioEscolar, mes, 15);
    }

    private LocalDate calcularFechaVencimiento(AnioEscolar anioEscolar, int mes, int dia) {
        int year = anioEscolar.getAnio();
        // Ensure the day is valid for the month
        int maxDay = LocalDate.of(year, mes, 1).lengthOfMonth();
        int safeDay = Math.min(dia, maxDay);
        return LocalDate.of(year, mes, safeDay);
    }

    public BigDecimal getTotalPendiente(UUID anioEscolarId) {
        Long count = pensionRepository.countByAnioEscolarIdAndEstado(anioEscolarId, PensionEstado.PENDIENTE);
        return BigDecimal.valueOf(count != null ? count : 0);
    }

    public BigDecimal getTotalRecaudado(UUID anioEscolarId) {
        return pensionRepository.sumMontoFinalPagadoByAnioEscolar(anioEscolarId);
    }
}
