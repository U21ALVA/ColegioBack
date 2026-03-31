package pe.edu.colegioricardopalma.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "exportacion_siagie")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportacionSiagie {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @Column(name = "periodo", length = 100)
    private String periodo;

    @Column(name = "archivo_url", length = 500)
    private String archivoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}
