package pe.edu.colegioricardopalma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.PageResponse;
import pe.edu.colegioricardopalma.dto.PagoDto;
import pe.edu.colegioricardopalma.entity.Pago;
import pe.edu.colegioricardopalma.entity.PagoEstado;
import pe.edu.colegioricardopalma.entity.Pension;
import pe.edu.colegioricardopalma.entity.PensionEstado;
import pe.edu.colegioricardopalma.repository.PagoRepository;
import pe.edu.colegioricardopalma.repository.PensionRepository;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PensionRepository pensionRepository;

    public List<PagoDto> findAll() {
        return pagoRepository.findAll().stream()
                .map(PagoDto::fromEntity)
                .collect(Collectors.toList());
    }

    public PageResponse<PagoDto> findWithFilters(PagoEstado estado, Pageable pageable) {
        Page<PagoDto> page = pagoRepository.findActiveWithFilters(estado, pageable)
                .map(PagoDto::fromEntity);
        return PageResponse.from(page);
    }

    public PagoDto findById(UUID id) {
        Pago pago = pagoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado: " + id));
        return PagoDto.fromEntity(pago);
    }

    public List<PagoDto> findByPension(UUID pensionId) {
        return pagoRepository.findByPensionId(pensionId).stream()
                .map(PagoDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public Pago createPago(Pension pension, String checkoutSessionId, BigDecimal monto) {
        Pago pago = Pago.builder()
                .pension(pension)
                .stripeCheckoutSessionId(checkoutSessionId)
                .monto(monto)
                .estado(PagoEstado.PENDIENTE)
                .build();

        Pago saved = pagoRepository.save(pago);
        log.info("Pago creado para pensión {} con sesión {}", pension.getId(), checkoutSessionId);
        return saved;
    }

    @Transactional
    public PagoDto updateFromWebhook(String sessionId, String paymentIntentId, 
                                      PagoEstado estado, String metodoPago, 
                                      Map<String, Object> metadata) {
        Pago pago = pagoRepository.findByStripeCheckoutSessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado para sesión: " + sessionId));

        pago.setStripePaymentIntentId(paymentIntentId);
        pago.setEstado(estado);
        pago.setMetodoPago(metodoPago);
        pago.setMetadata(metadata);

        if (estado == PagoEstado.COMPLETADO) {
            pago.setFechaPago(LocalDateTime.now());
            
            // Update pension status
            Pension pension = pago.getPension();
            pension.setEstado(PensionEstado.PAGADO);
            pensionRepository.save(pension);
            log.info("Pensión {} marcada como PAGADO", pension.getId());
        }

        Pago saved = pagoRepository.save(pago);
        log.info("Pago {} actualizado a estado {} desde webhook", pago.getId(), estado);

        return PagoDto.fromEntity(saved);
    }

    @Transactional
    public PagoDto updateEstado(UUID id, PagoEstado nuevoEstado) {
        Pago pago = pagoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado: " + id));

        pago.setEstado(nuevoEstado);
        
        if (nuevoEstado == PagoEstado.COMPLETADO && pago.getFechaPago() == null) {
            pago.setFechaPago(LocalDateTime.now());
        }

        Pago saved = pagoRepository.save(pago);
        log.info("Pago {} actualizado a estado {}", id, nuevoEstado);

        return PagoDto.fromEntity(saved);
    }

    public BigDecimal getTotalRecaudado(LocalDateTime desde, LocalDateTime hasta) {
        return pagoRepository.sumMontoByFechaRange(desde, hasta);
    }

    public BigDecimal getTotalRecaudadoAnioEscolar(UUID anioEscolarId) {
        return pagoRepository.sumMontoCompletadoByAnioEscolar(anioEscolarId);
    }

    public Long countCompletadosAnioEscolar(UUID anioEscolarId) {
        return pagoRepository.countByEstadoAndAnioEscolar(PagoEstado.COMPLETADO, anioEscolarId);
    }
}
