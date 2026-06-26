package pe.contrataia.seguimiento.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import pe.contrataia.proyecto.entity.Proyecto;
import pe.contrataia.shared.enums.TipoDocumento;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documentos")
@Getter
@Setter
@NoArgsConstructor
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoDocumento tipo;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(nullable = false)
    private boolean esPublico = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaSubida;
}
