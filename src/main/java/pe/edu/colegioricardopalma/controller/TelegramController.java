package pe.edu.colegioricardopalma.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.edu.colegioricardopalma.dto.*;
import pe.edu.colegioricardopalma.service.TelegramService;

@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
public class TelegramController {

    private final TelegramService telegramService;

    @PostMapping("/generar-codigo")
    @PreAuthorize("hasRole('PADRE')")
    public ResponseEntity<TelegramGenerarCodigoResponse> generarCodigo(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(telegramService.generarCodigo(userDetails.getUsername()));
    }

    @GetMapping("/vinculacion")
    @PreAuthorize("hasRole('PADRE')")
    public ResponseEntity<TelegramVinculacionDto> getVinculacion(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(telegramService.getVinculacion(userDetails.getUsername()));
    }

    @PostMapping("/vincular")
    public ResponseEntity<TelegramVinculacionDto> vincular(
            @Valid @RequestBody TelegramVincularRequest request
    ) {
        return ResponseEntity.ok(telegramService.vincular(request));
    }

    @GetMapping("/notas")
    public ResponseEntity<TelegramNotasResponse> notas(@RequestParam Long chatId) {
        return ResponseEntity.ok(telegramService.getNotasByChatId(chatId));
    }

    @GetMapping("/deudas")
    public ResponseEntity<TelegramDeudasResponse> deudas(@RequestParam Long chatId) {
        return ResponseEntity.ok(telegramService.getDeudasByChatId(chatId));
    }
}
