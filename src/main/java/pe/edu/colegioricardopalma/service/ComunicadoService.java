package pe.edu.colegioricardopalma.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.domain.PageRequest;
import pe.edu.colegioricardopalma.dto.ComunicadoCreateRequest;
import pe.edu.colegioricardopalma.dto.ComunicadoDto;
import pe.edu.colegioricardopalma.dto.ComunicadoEntregaDto;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.entity.*;
import pe.edu.colegioricardopalma.repository.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComunicadoService {

    private final ComunicadoRepository comunicadoRepository;
    private final ComunicadoEntregaRepository comunicadoEntregaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ApoderadoRepository apoderadoRepository;
    private final AlumnoApoderadoRepository alumnoApoderadoRepository;
    private final TelegramVinculacionRepository telegramVinculacionRepository;
    private final GradoRepository gradoRepository;

    @Value("${telegram.bot.token:}")
    private String telegramBotToken;

    @Transactional
    public ComunicadoDto createBorrador(ComunicadoCreateRequest request, String username) {
        Usuario creador = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + username));

        UUID[] destinoIds = resolveDestinoIdsForCreate(request);

        Comunicado comunicado = Comunicado.builder()
                .titulo(request.getTitulo())
                .contenido(request.getContenido())
                .adjuntoUrl(request.getAdjuntoUrl())
                .destinoTipo(request.getDestinoTipo())
                .destinoIds(destinoIds)
                .esReunion(Boolean.TRUE.equals(request.getEsReunion()))
                .fechaReunionInicio(request.getFechaReunionInicio())
                .fechaReunionFin(request.getFechaReunionFin())
                .lugarReunion(request.getLugarReunion())
                .estado(ComunicadoEstado.BORRADOR)
                .createdBy(creador)
                .build();

        return ComunicadoDto.fromEntity(comunicadoRepository.save(comunicado));
    }

    public PageResponse<ComunicadoDto> listAdmin(Pageable pageable) {
        Page<ComunicadoDto> page = comunicadoRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(ComunicadoDto::fromEntity);
        return PageResponse.from(page);
    }

    public PageResponse<ComunicadoDto> listParent(String username, int page, int size) {
        Apoderado apoderado = findApoderadoByUsername(username);
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        var entregasPage = comunicadoEntregaRepository
                .findByApoderadoIdOrderByComunicadoFecha(apoderado.getId(), pageable);

        var comunicados = entregasPage.getContent().stream()
                .map(ComunicadoEntrega::getComunicado)
                .filter(c -> c.getEstado() == ComunicadoEstado.PUBLICADO)
                .map(ComunicadoDto::fromEntity)
                .toList();

        return PageResponse.<ComunicadoDto>builder()
                .content(comunicados)
                .page(entregasPage.getNumber())
                .size(entregasPage.getSize())
                .totalElements(entregasPage.getTotalElements())
                .totalPages(entregasPage.getTotalPages())
                .first(entregasPage.isFirst())
                .last(entregasPage.isLast())
                .build();
    }

    public List<ComunicadoEntregaDto> listEntregas(UUID comunicadoId) {
        return comunicadoEntregaRepository.findByComunicadoIdWithApoderado(comunicadoId)
                .stream()
                .map(ComunicadoEntregaDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ComunicadoDto publicar(UUID comunicadoId) {
        Comunicado comunicado = comunicadoRepository.findByIdWithCreator(comunicadoId)
                .orElseThrow(() -> new EntityNotFoundException("Comunicado no encontrado: " + comunicadoId));

        if (comunicado.getEstado() != ComunicadoEstado.BORRADOR) {
            throw new IllegalStateException("Solo se pueden publicar comunicados en BORRADOR");
        }

        List<Apoderado> destinatarios = resolverDestinatarios(comunicado.getDestinoTipo(), comunicado.getDestinoIds());
        if (destinatarios.isEmpty()) {
            throw new IllegalStateException("No se encontraron destinatarios para el comunicado");
        }

        comunicado.setEstado(ComunicadoEstado.PUBLICADO);
        comunicado.setFechaPublicacion(LocalDateTime.now());
        comunicadoRepository.save(comunicado);

        for (Apoderado apoderado : destinatarios) {
            if (comunicadoEntregaRepository.existsByComunicadoIdAndApoderadoId(comunicado.getId(), apoderado.getId())) {
                continue;
            }

            ComunicadoEntrega entrega = ComunicadoEntrega.builder()
                    .comunicado(comunicado)
                    .apoderado(apoderado)
                    .entregado(false)
                    .leido(false)
                    .build();

            Optional<TelegramVinculacion> vinculacionOpt = telegramVinculacionRepository
                    .findFirstByApoderadoIdOrderByCreatedAtDesc(apoderado.getId());
            if (vinculacionOpt.isPresent()
                    && Boolean.TRUE.equals(vinculacionOpt.get().getVerificado())
                    && vinculacionOpt.get().getTelegramChatId() != null) {
                String texto = buildTelegramMessage(comunicado);
                try {
                    Long messageId = sendTelegramMessage(vinculacionOpt.get().getTelegramChatId(), texto);
                    entrega.setTelegramMessageId(messageId);
                    entrega.setEntregado(true);
                    entrega.setFechaEntrega(LocalDateTime.now());
                    entrega.setErrorMensaje(null);
                } catch (Exception ex) {
                    log.warn("No se pudo enviar comunicado {} a chat {}: {}",
                            comunicado.getId(), vinculacionOpt.get().getTelegramChatId(), ex.getMessage());
                    entrega.setErrorMensaje("No se pudo enviar por Telegram: " + ex.getMessage());
                }
            } else {
                entrega.setErrorMensaje("Apoderado sin Telegram vinculado");
            }

            comunicadoEntregaRepository.save(entrega);
        }

        return ComunicadoDto.fromEntity(comunicado);
    }

    private List<Apoderado> resolverDestinatarios(ComunicadoDestino destino, UUID[] destinoIdsArray) {
        Set<UUID> ids = new LinkedHashSet<>();
        List<UUID> destinoIds = destinoIdsArray != null ? Arrays.asList(destinoIdsArray) : List.of();

        switch (destino) {
            case TODOS -> apoderadoRepository.findActivos()
                    .forEach(a -> ids.add(a.getId()));

            case GRADO -> {
                if (destinoIds.isEmpty()) {
                    throw new IllegalArgumentException("Para destino GRADO se requieren destinoIds");
                }
                alumnoApoderadoRepository.findDistinctApoderadosByGradoIds(destinoIds)
                        .forEach(a -> ids.add(a.getId()));
            }

            case SECCION -> {
                if (destinoIds.isEmpty()) {
                    throw new IllegalArgumentException("Para destino SECCION se requieren destinoIds");
                }
                alumnoApoderadoRepository.findDistinctApoderadosBySeccionIds(destinoIds)
                        .forEach(a -> ids.add(a.getId()));
            }

            case NIVEL -> {
                if (destinoIds.isEmpty()) {
                    throw new IllegalArgumentException("Para destino NIVEL se requieren IDs de grado resueltos");
                }
                alumnoApoderadoRepository.findDistinctApoderadosByGradoIds(destinoIds)
                        .forEach(a -> ids.add(a.getId()));
            }
        }

        return ids.stream()
                .map(apoderadoRepository::findById)
                .flatMap(Optional::stream)
                .filter(a -> a.getEstado() == Estado.ACTIVO)
                .collect(Collectors.toList());
    }

    private String buildTelegramMessage(Comunicado comunicado) {
        StringBuilder sb = new StringBuilder();
        sb.append("📢 *Nuevo Comunicado*\n\n");
        sb.append("*" ).append(comunicado.getTitulo()).append("*\n\n");
        sb.append(comunicado.getContenido());
        if (comunicado.getAdjuntoUrl() != null && !comunicado.getAdjuntoUrl().isBlank()) {
            sb.append("\n\n📎 ").append(comunicado.getAdjuntoUrl());
        }
        return sb.toString();
    }

    private Long sendTelegramMessage(Long chatId, String text) {
        if (telegramBotToken == null || telegramBotToken.isBlank() || "xxx".equalsIgnoreCase(telegramBotToken)) {
            throw new IllegalStateException("Bot de Telegram no configurado en backend");
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.telegram.org/bot" + telegramBotToken + "/sendMessage";

        Map<String, Object> request = new HashMap<>();
        request.put("chat_id", chatId);
        request.put("text", text);
        request.put("parse_mode", "Markdown");

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Respuesta no exitosa de Telegram");
        }

        Map body = response.getBody();
        if (body == null || !Boolean.TRUE.equals(body.get("ok"))) {
            throw new IllegalStateException("Telegram devolvió respuesta inválida");
        }

        Object result = body.get("result");
        if (result instanceof Map resultMap) {
            Object messageId = resultMap.get("message_id");
            if (messageId instanceof Number n) {
                return n.longValue();
            }
        }
        return null;
    }

    private UUID[] resolveDestinoIdsForCreate(ComunicadoCreateRequest request) {
        if (request.getDestinoTipo() == ComunicadoDestino.TODOS) {
            return null;
        }

        if (request.getDestinoTipo() == ComunicadoDestino.NIVEL) {
            if (request.getNiveles() != null && !request.getNiveles().isEmpty()) {
                Set<UUID> gradoIds = new LinkedHashSet<>();
                for (Nivel nivel : request.getNiveles()) {
                    gradoRepository.findByNivelOrderByOrdenAsc(nivel)
                            .forEach(g -> {
                                if (g.getEstado() == Estado.ACTIVO) {
                                    gradoIds.add(g.getId());
                                }
                            });
                }
                if (gradoIds.isEmpty()) {
                    throw new IllegalArgumentException("No se encontraron grados activos para los niveles enviados");
                }
                return gradoIds.toArray(UUID[]::new);
            }

            // Compatibilidad con clientes existentes: permitir enviar IDs de grado directamente.
            if (request.getDestinoIds() != null && !request.getDestinoIds().isEmpty()) {
                return request.getDestinoIds().toArray(UUID[]::new);
            }

            throw new IllegalArgumentException("Para destino NIVEL debes enviar niveles o destinoIds (grados)");
        }

        if (request.getDestinoIds() == null || request.getDestinoIds().isEmpty()) {
            throw new IllegalArgumentException("Para destino " + request.getDestinoTipo() + " se requieren destinoIds");
        }
        return request.getDestinoIds().toArray(UUID[]::new);
    }

    private Apoderado findApoderadoByUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + username));
        return apoderadoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new EntityNotFoundException("Apoderado no encontrado para usuario actual"));
    }
}
