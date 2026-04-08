package pe.edu.colegioricardopalma.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "rol_tipo") // Nombre exacto que pusiste en el SQL
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "estado_tipo") // Nombre exacto que pusiste en el SQL
    @Builder.Default
    private Estado estado = Estado.ACTIVO;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Rol {
        ADMIN, PROFESOR, PADRE
    }

    public enum Estado {
        ACTIVO, INACTIVO, PENDIENTE, ELIMINADO
    }
}
