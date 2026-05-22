package pe.edu.colegioricardopalma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.BimestreDto;
import pe.edu.colegioricardopalma.entity.AnioEscolar;
import pe.edu.colegioricardopalma.entity.Bimestre;
import pe.edu.colegioricardopalma.repository.AnioEscolarRepository;
import pe.edu.colegioricardopalma.repository.BimestreRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BimestreService {

    private final BimestreRepository bimestreRepository;
    private final AnioEscolarRepository anioEscolarRepository;

    public List<BimestreDto> findByAnioEscolar(UUID anioEscolarId) {
        return bimestreRepository.findByAnioEscolarIdWithAnioEscolarOrderByNumeroAsc(anioEscolarId)
                .stream()
                .map(BimestreDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<BimestreDto> findByAnioActivo() {
        return bimestreRepository.findByAnioEscolarActivo()
                .stream()
                .map(BimestreDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<BimestreDto> findAbiertosAnioActivo() {
        return bimestreRepository.findBimestresAbiertosAnioActivo()
                .stream()
                .map(BimestreDto::fromEntity)
                .collect(Collectors.toList());
    }

    public BimestreDto findById(UUID id) {
        Bimestre bimestre = bimestreRepository.findByIdWithAnioEscolar(id)
                .orElseThrow(() -> new EntityNotFoundException("Bimestre no encontrado: " + id));
        return BimestreDto.fromEntity(bimestre);
    }

    @Transactional
    public BimestreDto create(BimestreDto dto) {
        AnioEscolar anioEscolar = anioEscolarRepository.findById(dto.getAnioEscolarId())
                .orElseThrow(() -> new EntityNotFoundException("Año escolar no encontrado: " + dto.getAnioEscolarId()));

        if (bimestreRepository.existsByNumeroAndAnioEscolarId(dto.getNumero(), anioEscolar.getId())) {
            throw new IllegalArgumentException("Ya existe el bimestre " + dto.getNumero() + " para el año escolar " + anioEscolar.getAnio());
        }

        Bimestre bimestre = Bimestre.builder()
                .numero(dto.getNumero())
                .anioEscolar(anioEscolar)
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .cerrado(false)
                .build();

        Bimestre saved = bimestreRepository.save(bimestre);
        log.info("Bimestre creado: {} para año {}", saved.getNumero(), anioEscolar.getAnio());
        
        return BimestreDto.fromEntity(saved);
    }

    @Transactional
    public BimestreDto update(UUID id, BimestreDto dto) {
        Bimestre bimestre = bimestreRepository.findByIdWithAnioEscolar(id)
                .orElseThrow(() -> new EntityNotFoundException("Bimestre no encontrado: " + id));

        if (bimestre.getCerrado()) {
            throw new IllegalStateException("No se puede modificar un bimestre cerrado");
        }

        bimestre.setFechaInicio(dto.getFechaInicio());
        bimestre.setFechaFin(dto.getFechaFin());
        
        Bimestre saved = bimestreRepository.save(bimestre);
        log.info("Bimestre actualizado: {}", saved.getNumero());
        
        return BimestreDto.fromEntity(saved);
    }

    @Transactional
    public BimestreDto cerrar(UUID id) {
        Bimestre bimestre = bimestreRepository.findByIdWithAnioEscolar(id)
                .orElseThrow(() -> new EntityNotFoundException("Bimestre no encontrado: " + id));

        if (bimestre.getCerrado()) {
            throw new IllegalStateException("El bimestre ya está cerrado");
        }

        // Check that previous bimesters are closed
        for (int i = 1; i < bimestre.getNumero(); i++) {
            Bimestre anterior = bimestreRepository.findByNumeroAndAnioEscolarId(i, bimestre.getAnioEscolar().getId())
                    .orElse(null);
            if (anterior != null && !anterior.getCerrado()) {
                throw new IllegalStateException("Debe cerrar el bimestre " + i + " antes de cerrar el bimestre " + bimestre.getNumero());
            }
        }

        bimestre.setCerrado(true);
        Bimestre saved = bimestreRepository.save(bimestre);
        log.info("Bimestre cerrado: {} del año {}", saved.getNumero(), bimestre.getAnioEscolar().getAnio());
        
        return BimestreDto.fromEntity(saved);
    }

    @Transactional
    public BimestreDto reabrir(UUID id) {
        Bimestre bimestre = bimestreRepository.findByIdWithAnioEscolar(id)
                .orElseThrow(() -> new EntityNotFoundException("Bimestre no encontrado: " + id));

        if (!bimestre.getCerrado()) {
            throw new IllegalStateException("El bimestre no está cerrado");
        }

        // Check that later bimesters are not closed
        for (int i = bimestre.getNumero() + 1; i <= 4; i++) {
            Bimestre siguiente = bimestreRepository.findByNumeroAndAnioEscolarId(i, bimestre.getAnioEscolar().getId())
                    .orElse(null);
            if (siguiente != null && siguiente.getCerrado()) {
                throw new IllegalStateException("Debe reabrir el bimestre " + i + " antes de reabrir el bimestre " + bimestre.getNumero());
            }
        }

        bimestre.setCerrado(false);
        Bimestre saved = bimestreRepository.save(bimestre);
        log.info("Bimestre reabierto: {} del año {}", saved.getNumero(), bimestre.getAnioEscolar().getAnio());
        
        return BimestreDto.fromEntity(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Bimestre bimestre = bimestreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bimestre no encontrado: " + id));

        if (bimestre.getCerrado()) {
            throw new IllegalStateException("No se puede eliminar un bimestre cerrado");
        }

        bimestreRepository.delete(bimestre);
        log.info("Bimestre eliminado: {}", bimestre.getNumero());
    }
}
