package pe.contrataia.empresa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import pe.contrataia.auth.entity.Empresa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "experiencias_empresa")
@Getter
@Setter
@NoArgsConstructor
public class ExperienciaEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String entidadContratante;

    @Column(precision = 15, scale = 2)
    private BigDecimal monto;

    private String rubro;
    private String region;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
