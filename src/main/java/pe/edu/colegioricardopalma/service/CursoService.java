package pe.edu.colegioricardopalma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.CursoDto;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.entity.Curso;
import pe.edu.colegioricardopalma.entity.Nivel;
import pe.edu.colegioricardopalma.repository.CursoRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursoRepository;

    public List<CursoDto> findAll() {
        return cursoRepository.findAllActiveOrderByNivelAndNombre()
                .stream()
                .map(CursoDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<CursoDto> findByNivel(Nivel nivel) {
        return cursoRepository.findByNivelActivos(nivel)
                .stream()
                .map(CursoDto::fromEntity)
                .collect(Collectors.toList());
    }

    public PageResponse<CursoDto> findAllPaginated(Pageable pageable) {
        Page<CursoDto> page = cursoRepository.findActivos(pageable)
                .map(CursoDto::fromEntity);
        return PageResponse.from(page);
    }

    public PageResponse<CursoDto> search(String search, Nivel nivel, Pageable pageable) {
        String safeSearch = search != null ? search : "";
        Page<CursoDto> page;
        if (nivel != null) {
            page = cursoRepository.searchCursosByNivel(safeSearch, nivel, pageable)
                    .map(CursoDto::fromEntity);
        } else {
            page = cursoRepository.searchCursos(safeSearch, pageable)
                    .map(CursoDto::fromEntity);
        }
        return PageResponse.from(page);
    }

    public CursoDto findById(UUID id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Curso no encontrado: " + id));
        return CursoDto.fromEntity(curso);
    }

    @Transactional
    public CursoDto create(CursoDto dto) {
        if (cursoRepository.existsByNombreAndNivel(dto.getNombre(), dto.getNivel())) {
            throw new IllegalArgumentException("Ya existe un curso con ese nombre para el nivel seleccionado");
        }

        Curso curso = dto.toEntity();
        Curso saved = cursoRepository.save(curso);
        log.info("Curso creado: {} ({})", saved.getNombre(), saved.getNivel());
        
        return CursoDto.fromEntity(saved);
    }

    @Transactional
    public CursoDto update(UUID id, CursoDto dto) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Curso no encontrado: " + id));

        // Check for duplicate if name or nivel changed
        if ((!curso.getNombre().equals(dto.getNombre()) || !curso.getNivel().equals(dto.getNivel())) 
                && cursoRepository.existsByNombreAndNivel(dto.getNombre(), dto.getNivel())) {
            throw new IllegalArgumentException("Ya existe un curso con ese nombre para el nivel seleccionado");
        }

        curso.setNombre(dto.getNombre());
        curso.setNivel(dto.getNivel());
        if (dto.getEstado() != null) {
            curso.setEstado(dto.getEstado());
        }
        
        Curso saved = cursoRepository.save(curso);
        log.info("Curso actualizado: {} ({})", saved.getNombre(), saved.getNivel());
        
        return CursoDto.fromEntity(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Curso no encontrado: " + id));

        curso.setEstado(pe.edu.colegioricardopalma.entity.Estado.ELIMINADO);
        cursoRepository.save(curso);
        log.info("Curso eliminado (soft): {} ({})", curso.getNombre(), curso.getNivel());
    }
}
