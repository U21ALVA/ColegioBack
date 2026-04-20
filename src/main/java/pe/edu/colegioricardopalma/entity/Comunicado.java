package pe.edu.colegioricardopalma.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "comunicado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comunicado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "adjunto_url", length = 500)
    private String adjuntoUrl;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "destino_tipo", nullable = false, columnDefinition = "comunicado_destino")
    private ComunicadoDestino destinoTipo;

    @Column(name = "destino_ids", columnDefinition = "uuid[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private UUID[] destinoIds;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false, columnDefinition = "comunicado_estado")
    @Builder.Default
    private ComunicadoEstado estado = ComunicadoEstado.BORRADOR;

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @Column(name = "es_reunion", nullable = false)
    @Builder.Default
    private Boolean esReunion = false;

    @Column(name = "fecha_reunion_inicio")
    private LocalDateTime fechaReunionInicio;

    @Column(name = "fecha_reunion_fin")
    private LocalDateTime fechaReunionFin;

    @Column(name = "lugar_reunion", length = 200)
    private String lugarReunion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Usuario createdBy;

    @OneToMany(mappedBy = "comunicado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComunicadoEntrega> entregas;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
