package pe.edu.colegioricardopalma.controller;

import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.edu.colegioricardopalma.dto.PensionDto;
import pe.edu.colegioricardopalma.dto.StripeCheckoutRequest;
import pe.edu.colegioricardopalma.dto.StripeCheckoutResponse;
import pe.edu.colegioricardopalma.repository.AlumnoApoderadoRepository;
import pe.edu.colegioricardopalma.repository.ApoderadoRepository;
import pe.edu.colegioricardopalma.repository.UsuarioRepository;
import pe.edu.colegioricardopalma.service.PensionService;
import pe.edu.colegioricardopalma.service.StripeService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
public class StripeController {

    private final StripeService stripeService;
    private final PensionService pensionService;
    private final UsuarioRepository usuarioRepository;
    private final ApoderadoRepository apoderadoRepository;
    private final AlumnoApoderadoRepository alumnoApoderadoRepository;

    @Value("${stripe.publishable-key:pk_test_xxx}")
    private String publishableKey;

    /**
     * Returns Stripe publishable key for frontend initialization.
     */
    @GetMapping("/config")
    @PreAuthorize("hasAnyRole('ADMIN', 'PADRE')")
    public ResponseEntity<Map<String, String>> getConfig() {
        return ResponseEntity.ok(Map.of("publishableKey", publishableKey));
    }

    /**
     * Creates a Stripe Checkout Session for a pension payment.
     * PADRE role can only pay for their own children's pensions.
     */
    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('ADMIN', 'PADRE')")
    public ResponseEntity<?> createCheckoutSession(
            @Valid @RequestBody StripeCheckoutRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Verify PADRE can only pay for their children's pensions
        if (isPadreRole(userDetails)) {
            PensionDto pension = pensionService.findById(request.getPensionId());
            List<UUID> hijosIds = getHijosIds(userDetails);
            
            if (!hijosIds.contains(pension.getAlumnoId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "No tiene permiso para pagar esta pensión"));
            }
        }

        try {
            StripeCheckoutResponse response = stripeService.createCheckoutSession(request);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            log.error("Error creating Stripe checkout session", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Error al crear sesión de pago: " + e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Stripe webhook endpoint - receives payment events.
     * NO AUTHENTICATION - Stripe signs the request.
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        try {
            stripeService.handleWebhook(payload, sigHeader);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Webhook signature verification failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid signature"));
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error processing webhook"));
        }
    }

    /**
     * Verify a checkout session status.
     */
    @GetMapping("/verify/{sessionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PADRE')")
    public ResponseEntity<Map<String, Object>> verifySession(@PathVariable String sessionId) {
        try {
            boolean isPaid = stripeService.verifySessionPayment(sessionId);
            return ResponseEntity.ok(Map.of(
                    "sessionId", sessionId,
                    "paid", isPaid
            ));
        } catch (StripeException e) {
            log.error("Error verifying session", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Error verificando sesión: " + e.getMessage()));
        }
    }

    private boolean isPadreRole(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PADRE"));
    }

    private List<UUID> getHijosIds(UserDetails userDetails) {
        return usuarioRepository.findByUsername(userDetails.getUsername())
                .flatMap(usuario -> apoderadoRepository.findByUsuarioId(usuario.getId()))
                .map(apoderado -> alumnoApoderadoRepository.findByApoderadoId(apoderado.getId()).stream()
                        .map(aa -> aa.getAlumno().getId())
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }
}
