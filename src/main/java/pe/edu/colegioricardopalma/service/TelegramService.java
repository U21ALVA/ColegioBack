package pe.edu.colegioricardopalma.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.*;
import pe.edu.colegioricardopalma.entity.*;
import pe.edu.colegioricardopalma.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramService {

    private final UsuarioRepository usuarioRepository;
    private final ApoderadoRepository apoderadoRepository;
    private final AlumnoApoderadoRepository alumnoApoderadoRepository;
    private final TelegramVinculacionRepository telegramVinculacionRepository;
    private final NotaRepository notaRepository;
    private final PensionService pensionService;

    @Transactional
    public TelegramGenerarCodigoResponse generarCodigo(String username) {
        Apoderado apoderado = getApoderadoByUsername(username);
        TelegramVinculacion vinculacion = telegramVinculacionRepository.findFirstByApoderadoIdOrderByCreatedAtDesc(apoderado.getId())
                .orElse(TelegramVinculacion.builder().apoderado(apoderado).build());

        String codigo = generarCodigoAleatorio();
        LocalDateTime expiraAt = LocalDateTime.now().plusMinutes(15);

        vinculacion.setCodigoVerificacion(codigo);
        vinculacion.setCodigoExpiraAt(expiraAt);
        vinculacion.setVerificado(false);
        vinculacion.setTelegramChatId(null);
        vinculacion.setFechaVinculacion(null);

        telegramVinculacionRepository.save(vinculacion);
        return TelegramGenerarCodigoResponse.builder()
                .codigo(codigo)
                .expiraAt(expiraAt)
                .build();
    }

    public TelegramVinculacionDto getVinculacion(String username) {
        Apoderado apoderado = getApoderadoByUsername(username);
        return telegramVinculacionRepository.findFirstByApoderadoIdOrderByCreatedAtDesc(apoderado.getId())
                .map(TelegramVinculacionDto::fromEntity)
                .orElse(TelegramVinculacionDto.builder().verificado(false).build());
    }

    @Transactional
    public TelegramVinculacionDto vincular(TelegramVincularRequest request) {
        TelegramVinculacion vinculacion = telegramVinculacionRepository.findByCodigoVerificacion(request.getCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Código inválido"));

        if (vinculacion.getCodigoExpiraAt() == null || vinculacion.getCodigoExpiraAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("El código de vinculación está expirado");
        }

        Optional<TelegramVinculacion> existenteChat = telegramVinculacionRepository.findByTelegramChatId(request.getChatId());
        if (existenteChat.isPresent() && !existenteChat.get().getId().equals(vinculacion.getId())) {
            throw new IllegalStateException("Este chat ya está vinculado a otra cuenta");
        }

        vinculacion.setTelegramChatId(request.getChatId());
        vinculacion.setVerificado(true);
        vinculacion.setFechaVinculacion(LocalDateTime.now());
        vinculacion.setCodigoVerificacion(null);
        vinculacion.setCodigoExpiraAt(null);

        telegramVinculacionRepository.save(vinculacion);
        return TelegramVinculacionDto.fromEntity(vinculacion);
    }

    @Transactional
    public TelegramVinculacionDto desvincular(String username) {
        Apoderado apoderado = getApoderadoByUsername(username);
        TelegramVinculacion vinculacion = telegramVinculacionRepository
                .findFirstByApoderadoIdOrderByCreatedAtDesc(apoderado.getId())
                .orElseThrow(() -> new EntityNotFoundException("No existe una vinculación para este apoderado"));

        vinculacion.setTelegramChatId(null);
        vinculacion.setVerificado(false);
        vinculacion.setFechaVinculacion(null);
        vinculacion.setCodigoVerificacion(null);
        vinculacion.setCodigoExpiraAt(null);

        telegramVinculacionRepository.save(vinculacion);
        return TelegramVinculacionDto.fromEntity(vinculacion);
    }

    public TelegramNotasResponse getNotasByChatId(Long chatId) {
        Apoderado apoderado = validarChatVinculado(chatId);
        List<AlumnoApoderado> alumnosRelacion = alumnoApoderadoRepository.findByApoderadoIdWithAlumno(apoderado.getId());

        List<TelegramNotasResponse.AlumnoNotasResumen> alumnos = alumnosRelacion.stream()
                .map(AlumnoApoderado::getAlumno)
                .filter(Objects::nonNull)
                .map(alumno -> {
                    List<Nota> notas = notaRepository.findByAlumnoIdWithDetails(alumno.getId());
                    BigDecimal promedio = calcularPromedioNotas(notas);

                    return TelegramNotasResponse.AlumnoNotasResumen.builder()
                            .alumnoId(alumno.getId().toString())
                            .alumnoNombre(alumno.getNombreCompleto())
                            .grado(alumno.getGrado() != null ? alumno.getGrado().getNombre() : "-")
                            .seccion(alumno.getSeccion() != null ? alumno.getSeccion().getNombre() : "-")
                            .totalNotas(notas.size())
                            .promedio(promedio != null ? promedio.toString() : "-")
                            .build();
                })
                .collect(Collectors.toList());

        return TelegramNotasResponse.builder()
                .mensaje(alumnos.isEmpty() ? "No se encontraron notas para tus hijos" : "Notas obtenidas correctamente")
                .alumnos(alumnos)
                .build();
    }

    public TelegramDeudasResponse getDeudasByChatId(Long chatId) {
        Apoderado apoderado = validarChatVinculado(chatId);
        List<AlumnoApoderado> alumnosRelacion = alumnoApoderadoRepository.findByApoderadoIdWithAlumno(apoderado.getId());
        List<UUID> alumnoIds = alumnosRelacion.stream()
                .map(AlumnoApoderado::getAlumno)
                .filter(Objects::nonNull)
                .map(Alumno::getId)
                .collect(Collectors.toList());

        List<PensionDto> pensionesPendientes = pensionService.findPendientesByAlumnos(alumnoIds);
        Map<UUID, List<PensionDto>> porAlumno = pensionesPendientes.stream()
                .collect(Collectors.groupingBy(PensionDto::getAlumnoId));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<TelegramDeudasResponse.AlumnoDeudasResumen> resumen = alumnosRelacion.stream()
                .map(AlumnoApoderado::getAlumno)
                .filter(Objects::nonNull)
                .map(alumno -> {
                    List<PensionDto> deudas = porAlumno.getOrDefault(alumno.getId(), List.of());
                    BigDecimal total = deudas.stream()
                            .map(PensionDto::getMontoFinal)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    List<TelegramDeudasResponse.DetalleDeuda> detalle = deudas.stream()
                            .map(p -> TelegramDeudasResponse.DetalleDeuda.builder()
                                    .pensionId(p.getId().toString())
                                    .mes(p.getNombreMes())
                                    .estado(p.getEstado() != null ? p.getEstado().name() : "-")
                                    .monto(p.getMontoFinal() != null ? p.getMontoFinal().toString() : "0.00")
                                    .vencimiento(p.getFechaVencimiento() != null ? p.getFechaVencimiento().format(formatter) : "-")
                                    .build())
                            .collect(Collectors.toList());

                    return TelegramDeudasResponse.AlumnoDeudasResumen.builder()
                            .alumnoId(alumno.getId().toString())
                            .alumnoNombre(alumno.getNombreCompleto())
                            .totalPendientes(deudas.size())
                            .montoTotalPendiente(total.toString())
                            .deudas(detalle)
                            .build();
                })
                .collect(Collectors.toList());

        return TelegramDeudasResponse.builder()
                .mensaje("Estado de deudas obtenido correctamente")
                .alumnos(resumen)
                .build();
    }

    private Apoderado validarChatVinculado(Long chatId) {
        TelegramVinculacion vinculacion = telegramVinculacionRepository.findByTelegramChatIdWithApoderado(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat no vinculado"));

        if (!Boolean.TRUE.equals(vinculacion.getVerificado())) {
            throw new SecurityException("Chat no verificado");
        }

        if (vinculacion.getApoderado() == null) {
            throw new EntityNotFoundException("No existe apoderado asociado al chat");
        }

        return vinculacion.getApoderado();
    }

    private Apoderado getApoderadoByUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + username));
        return apoderadoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new EntityNotFoundException("Apoderado no encontrado para usuario actual"));
    }

    private String generarCodigoAleatorio() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private BigDecimal calcularPromedioNotas(List<Nota> notas) {
        List<BigDecimal> finals = notas.stream()
                .map(Nota::getNotaFinal)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (finals.isEmpty()) {
            return null;
        }

        BigDecimal suma = finals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return suma.divide(BigDecimal.valueOf(finals.size()), 2, RoundingMode.HALF_UP);
    }
}
