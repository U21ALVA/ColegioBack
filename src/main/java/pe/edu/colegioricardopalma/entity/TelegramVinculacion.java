package pe.edu.colegioricardopalma.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "telegram_vinculacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramVinculacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apoderado_id", nullable = false)
    private Apoderado apoderado;

    @Column(name = "telegram_chat_id", unique = true)
    private Long telegramChatId;

    @Column(name = "codigo_verificacion", length = 10)
    private String codigoVerificacion;

    @Column(name = "codigo_expira_at")
    private LocalDateTime codigoExpiraAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean verificado = false;

    @Column(name = "fecha_vinculacion")
    private LocalDateTime fechaVinculacion;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
