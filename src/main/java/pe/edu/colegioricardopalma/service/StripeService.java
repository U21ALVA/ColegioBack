package pe.edu.colegioricardopalma.service;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.colegioricardopalma.dto.StripeCheckoutRequest;
import pe.edu.colegioricardopalma.dto.StripeCheckoutResponse;
import pe.edu.colegioricardopalma.entity.*;
import pe.edu.colegioricardopalma.repository.PagoRepository;
import pe.edu.colegioricardopalma.repository.PensionRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeService {

    private final PensionRepository pensionRepository;
    private final PagoRepository pagoRepository;
    private final PagoService pagoService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Creates a Stripe Checkout Session for the given pension.
     */
    @Transactional
    public StripeCheckoutResponse createCheckoutSession(StripeCheckoutRequest request) throws StripeException {
        Pension pension = pensionRepository.findByIdWithDetails(request.getPensionId())
                .orElseThrow(() -> new EntityNotFoundException("Pensión no encontrada: " + request.getPensionId()));

        // Validate pension can be paid
        if (pension.getEstado() == PensionEstado.PAGADO) {
            throw new IllegalStateException("Esta pensión ya ha sido pagada");
        }

        Alumno alumno = pension.getAlumno();
        
        // Build description
        String description = String.format("Pensión %s %d - %s %s", 
                pension.getNombreMes(), 
                pension.getAnioEscolar().getAnio(),
                alumno.getNombres(),
                alumno.getApellidos());

        // Determine success and cancel URLs
        String baseUrl = frontendUrl.split(",")[0].trim();
        String successUrl = request.getSuccessUrl() != null 
                ? request.getSuccessUrl() 
                : baseUrl + "/padre/pagos/success?session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl = request.getCancelUrl() != null 
                ? request.getCancelUrl() 
                : baseUrl + "/padre/pagos/cancel";

        // Convert amount to cents (Stripe uses smallest currency unit)
        long amountInCents = pension.getMontoFinal()
                .multiply(java.math.BigDecimal.valueOf(100))
                .longValue();

        // Create Stripe Checkout Session
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("pen")
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(description)
                                                                .setDescription("Colegio Ricardo Palma - Pensión Escolar")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putMetadata("pension_id", pension.getId().toString())
                .putMetadata("alumno_id", alumno.getId().toString())
                .putMetadata("mes", String.valueOf(pension.getMes()))
                .putMetadata("anio_escolar", String.valueOf(pension.getAnioEscolar().getAnio()))
                .build();

        Session session = Session.create(params);

        // Create Pago record
        Pago pago = pagoService.createPago(pension, session.getId(), pension.getMontoFinal());

        log.info("Checkout session created: {} for pension {}", session.getId(), pension.getId());

        return StripeCheckoutResponse.builder()
                .checkoutUrl(session.getUrl())
                .sessionId(session.getId())
                .pagoId(pago.getId())
                .build();
    }

    /**
     * Verifies and processes a Stripe webhook event.
     */
    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        Event event;
        
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid signature");
        }

        log.info("Processing webhook event: {} ({})", event.getType(), event.getId());

        // Deserialize the event data
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = dataObjectDeserializer.getObject().orElse(null);

        if (stripeObject == null) {
            log.warn("Could not deserialize webhook data for event: {}", event.getId());
            return;
        }

        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted((Session) stripeObject);
                break;
            case "checkout.session.expired":
                handleCheckoutSessionExpired((Session) stripeObject);
                break;
            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded((PaymentIntent) stripeObject);
                break;
            case "payment_intent.payment_failed":
                handlePaymentIntentFailed((PaymentIntent) stripeObject);
                break;
            default:
                log.debug("Unhandled event type: {}", event.getType());
        }
    }

    private void handleCheckoutSessionCompleted(Session session) {
        log.info("Checkout session completed: {}", session.getId());

        String paymentIntentId = session.getPaymentIntent();
        String paymentMethodType = session.getPaymentMethodTypes() != null && !session.getPaymentMethodTypes().isEmpty()
                ? session.getPaymentMethodTypes().get(0)
                : "card";

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("customer_email", session.getCustomerEmail());
        metadata.put("payment_status", session.getPaymentStatus());
        if (session.getMetadata() != null) {
            metadata.putAll(session.getMetadata());
        }

        try {
            pagoService.updateFromWebhook(
                    session.getId(),
                    paymentIntentId,
                    PagoEstado.COMPLETADO,
                    paymentMethodType,
                    metadata
            );
            log.info("Payment completed for session: {}", session.getId());
        } catch (EntityNotFoundException e) {
            log.error("Pago not found for session: {}", session.getId());
        }
    }

    private void handleCheckoutSessionExpired(Session session) {
        log.info("Checkout session expired: {}", session.getId());

        try {
            pagoService.updateFromWebhook(
                    session.getId(),
                    null,
                    PagoEstado.FALLIDO,
                    null,
                    Map.of("reason", "session_expired")
            );
        } catch (EntityNotFoundException e) {
            log.warn("Pago not found for expired session: {}", session.getId());
        }
    }

    private void handlePaymentIntentSucceeded(PaymentIntent paymentIntent) {
        log.info("Payment intent succeeded: {}", paymentIntent.getId());
        // Most processing is done in checkout.session.completed
        // This is a backup confirmation
    }

    private void handlePaymentIntentFailed(PaymentIntent paymentIntent) {
        log.warn("Payment intent failed: {} - {}", 
                paymentIntent.getId(), 
                paymentIntent.getLastPaymentError() != null 
                        ? paymentIntent.getLastPaymentError().getMessage() 
                        : "Unknown error");

        // Try to find and update the pago by payment intent ID
        pagoRepository.findByStripePaymentIntentId(paymentIntent.getId())
                .ifPresent(pago -> {
                    pago.setEstado(PagoEstado.FALLIDO);
                    Map<String, Object> metadata = pago.getMetadata() != null 
                            ? new HashMap<>(pago.getMetadata()) 
                            : new HashMap<>();
                    metadata.put("failure_reason", paymentIntent.getLastPaymentError() != null 
                            ? paymentIntent.getLastPaymentError().getMessage() 
                            : "Payment failed");
                    pago.setMetadata(metadata);
                    pagoRepository.save(pago);
                    log.info("Pago {} marked as FALLIDO", pago.getId());
                });
    }

    /**
     * Verifies if a session ID corresponds to a completed payment.
     */
    public boolean verifySessionPayment(String sessionId) throws StripeException {
        Session session = Session.retrieve(sessionId);
        return "paid".equals(session.getPaymentStatus());
    }
}
