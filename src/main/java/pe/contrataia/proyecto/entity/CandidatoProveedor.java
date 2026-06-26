package pe.contrataia.proyecto.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import pe.contrataia.auth.entity.Empresa;
import pe.contrataia.shared.enums.EstadoCandidato;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "candidatos_proveedor",
    uniqueConstraints = @UniqueConstraint(columnNames = {"proyecto_id", "ruc"})
)
@Getter
@Setter
@NoArgsConstructor
public class CandidatoProveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    // Empresa registrada en el sistema (puede ser null si solo está en LatInfo)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @Column(nullable = false, length = 11)
    private String ruc;

    private String razonSocial;

    // Snapshot JSON de LatInfo almacenado como texto
    @Column(columnDefinition = "jsonb")
    private String snapshotLatinfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoCandidato estado = EstadoCandidato.CANDIDATO;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaAgregado;
}
