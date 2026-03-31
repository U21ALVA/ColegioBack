package pe.edu.colegioricardopalma.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nota_historial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_id", nullable = false)
    private Nota nota;

    @Column(name = "campo_modificado", length = 20)
    private String campoModificado;

    @Column(name = "valor_anterior", length = 10)
    private String valorAnterior;

    @Column(name = "valor_nuevo", length = 10)
    private String valorNuevo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
