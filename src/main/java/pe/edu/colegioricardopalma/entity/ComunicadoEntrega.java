package pe.edu.colegioricardopalma.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "comunicado_entrega", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"comunicado_id", "apoderado_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComunicadoEntrega {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comunicado_id", nullable = false)
    private Comunicado comunicado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apoderado_id", nullable = false)
    private Apoderado apoderado;

    @Column(name = "telegram_message_id")
    private Long telegramMessageId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean entregado = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean leido = false;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    @Column(name = "error_mensaje", columnDefinition = "TEXT")
    private String errorMensaje;
}
