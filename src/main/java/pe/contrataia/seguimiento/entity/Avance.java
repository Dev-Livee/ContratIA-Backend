package pe.contrataia.seguimiento.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import pe.contrataia.proyecto.entity.Proyecto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "avances")
@Getter
@Setter
@NoArgsConstructor
public class Avance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal avanceFisico;

    @Column(precision = 5, scale = 2)
    private BigDecimal avanceFinanciero;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaRegistro;
}
