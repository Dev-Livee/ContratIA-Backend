package pe.contrataia.seguimiento.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.contrataia.proyecto.entity.Proyecto;
import pe.contrataia.shared.enums.EstadoIncidencia;
import pe.contrataia.shared.enums.SeveridadIncidencia;
import pe.contrataia.shared.enums.TipoIncidencia;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "incidencias")
@Getter
@Setter
@NoArgsConstructor
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hito_id")
    private Hito hito;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoIncidencia tipo = TipoIncidencia.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeveridadIncidencia severidad = SeveridadIncidencia.MEDIA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoIncidencia estado = EstadoIncidencia.ABIERTA;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaReporte;

    private LocalDateTime fechaResolucion;

    @Column(columnDefinition = "TEXT")
    private String resolucion;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
