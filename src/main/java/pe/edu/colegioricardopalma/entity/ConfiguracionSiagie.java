package pe.edu.colegioricardopalma.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "configuracion_siagie")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionSiagie {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "institucion_educativa", nullable = false, length = 200)
    private String institucionEducativa;

    @Column(name = "codigo_modular_anexo", nullable = false, length = 50)
    private String codigoModularAnexo;

    @Column(name = "nivel", nullable = false, length = 20)
    private String nivel;

    @Column(name = "nombre_reporte", nullable = false, length = 200)
    private String nombreReporte;

    @Column(name = "anio_academico", nullable = false)
    private Integer anioAcademico;

    @Column(name = "diseno_modular", length = 100)
    private String disenoModular;

    @Column(name = "periodo", length = 50)
    private String periodo;

    @Column(name = "grado", length = 50)
    private String grado;

    @Column(name = "seccion", length = 20)
    private String seccion;

    @Column(name = "areas_cursos", columnDefinition = "TEXT")
    private String areasCursos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Usuario createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
