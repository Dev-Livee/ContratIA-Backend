package pe.contrataia.seguimiento.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import pe.contrataia.proyecto.entity.Proyecto;
import pe.contrataia.shared.enums.TipoEvidencia;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "evidencias")
@Getter
@Setter
@NoArgsConstructor
public class Evidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hito_id")
    private Hito hito;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEvidencia tipo;

    private String nombre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private boolean esPublico = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaSubida;
}
