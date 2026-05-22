package pe.edu.colegioricardopalma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.BecaCreateRequest;
import pe.edu.colegioricardopalma.dto.BecaDto;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.entity.Alumno;
import pe.edu.colegioricardopalma.entity.AnioEscolar;
import pe.edu.colegioricardopalma.entity.Beca;
import pe.edu.colegioricardopalma.entity.Pension;
import pe.edu.colegioricardopalma.entity.PensionEstado;
import pe.edu.colegioricardopalma.entity.Usuario;
import pe.edu.colegioricardopalma.repository.*;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BecaService {

    private final BecaRepository becaRepository;
    private final AlumnoRepository alumnoRepository;
    private final AnioEscolarRepository anioEscolarRepository;
    private final UsuarioRepository usuarioRepository;
    private final PensionRepository pensionRepository;

    public List<BecaDto> findAll() {
        return becaRepository.findAll().stream()
                .map(BecaDto::fromEntity)
                .collect(Collectors.toList());
    }

    public PageResponse<BecaDto> findWithFilters(String tipo, Boolean vigente, Pageable pageable) {
        Page<BecaDto> page = becaRepository.findActiveWithFilters(tipo, vigente, pageable)
                .map(BecaDto::fromEntity);
        return PageResponse.from(page);
    }

    public BecaDto findById(UUID id) {
        Beca beca = becaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Beca no encontrada: " + id));
        return BecaDto.fromEntity(beca);
    }

    public List<BecaDto> findByAlumno(UUID alumnoId) {
        return becaRepository.findByAlumnoId(alumnoId).stream()
                .map(BecaDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<BecaDto> findVigentesByAlumnoAndAnioEscolar(UUID alumnoId, UUID anioEscolarId) {
        return becaRepository.findVigentesByAlumnoAndAnioEscolar(alumnoId, anioEscolarId).stream()
                .map(BecaDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public BecaDto create(BecaCreateRequest request) {
        // Validate alumno
        Alumno alumno = alumnoRepository.findById(request.getAlumnoId())
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado: " + request.getAlumnoId()));

        // Validate año escolar
        AnioEscolar anioEscolar = anioEscolarRepository.findById(request.getAnioEscolarId())
                .orElseThrow(() -> new EntityNotFoundException("Año escolar no encontrado: " + request.getAnioEscolarId()));

        // Check for duplicate
        if (becaRepository.existsByAlumnoIdAndAnioEscolarIdAndTipo(
                request.getAlumnoId(), request.getAnioEscolarId(), request.getTipo())) {
            throw new IllegalArgumentException("Ya existe una beca de este tipo para este alumno y año escolar");
        }

        // Get current user
        Usuario aprobadoPor = getCurrentUsuario();

        Beca beca = Beca.builder()
                .alumno(alumno)
                .anioEscolar(anioEscolar)
                .tipo(request.getTipo().toUpperCase())
                .porcentaje(request.getPorcentaje())
                .motivo(request.getMotivo())
                .aprobadoPor(aprobadoPor)
                .vigente(true)
                .build();

        Beca saved = becaRepository.save(beca);
        recalcularPensionesAlumnoAnio(alumno.getId(), anioEscolar.getId());
        log.info("Beca {} creada para alumno {} - {}%", 
                request.getTipo(), alumno.getNombreCompleto(), request.getPorcentaje());

        return BecaDto.fromEntity(saved);
    }

    @Transactional
    public BecaDto update(UUID id, BecaCreateRequest request) {
        Beca beca = becaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Beca no encontrada: " + id));

        beca.setTipo(request.getTipo().toUpperCase());
        beca.setPorcentaje(request.getPorcentaje());
        beca.setMotivo(request.getMotivo());

        Beca saved = becaRepository.save(beca);
        recalcularPensionesAlumnoAnio(beca.getAlumno().getId(), beca.getAnioEscolar().getId());
        log.info("Beca {} actualizada", id);

        return BecaDto.fromEntity(saved);
    }

    @Transactional
    public BecaDto toggleVigencia(UUID id) {
        Beca beca = becaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Beca no encontrada: " + id));

        beca.setVigente(!beca.getVigente());
        Beca saved = becaRepository.save(beca);
        recalcularPensionesAlumnoAnio(beca.getAlumno().getId(), beca.getAnioEscolar().getId());
        log.info("Beca {} cambiada a vigente={}", id, saved.getVigente());

        return BecaDto.fromEntity(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Beca beca = becaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Beca no encontrada: " + id));
        UUID alumnoId = beca.getAlumno().getId();
        UUID anioId = beca.getAnioEscolar().getId();

        becaRepository.deleteById(id);
        recalcularPensionesAlumnoAnio(alumnoId, anioId);
        log.info("Beca eliminada: {}", id);
    }

    private void recalcularPensionesAlumnoAnio(UUID alumnoId, UUID anioEscolarId) {
        BigDecimal porcentaje = becaRepository.sumPorcentajeVigenteByAlumnoAndAnioEscolar(alumnoId, anioEscolarId);
        BigDecimal factor = (porcentaje == null ? BigDecimal.ZERO : porcentaje)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        List<Pension> pensiones = pensionRepository.findByAlumnoIdAndAnioEscolarId(alumnoId, anioEscolarId);
        for (Pension p : pensiones) {
            if (p.getEstado() == PensionEstado.PAGADO) {
                continue;
            }
            BigDecimal descuento = p.getMonto().multiply(factor).setScale(2, RoundingMode.HALF_UP);
            p.setDescuento(descuento);
            p.calcularMontoFinal();
        }
        pensionRepository.saveAll(pensiones);
    }

    private Usuario getCurrentUsuario() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            return usuarioRepository.findByUsername(username).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
