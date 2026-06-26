package pe.contrataia.seguimiento.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.contrataia.proyecto.entity.Proyecto;
import pe.contrataia.shared.enums.EstadoHito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "hitos")
@Getter
@Setter
@NoArgsConstructor
public class Hito {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private LocalDate fechaPrevista;
    private LocalDate fechaReal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoHito estado = EstadoHito.PENDIENTE;

    @Column(nullable = false)
    private Integer orden = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
