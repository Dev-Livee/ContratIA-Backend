package pe.contrataia.proyecto.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.contrataia.auth.entity.Empresa;
import pe.contrataia.auth.entity.EntidadPublica;
import pe.contrataia.seguimiento.entity.Avance;
import pe.contrataia.seguimiento.entity.Documento;
import pe.contrataia.seguimiento.entity.Evidencia;
import pe.contrataia.seguimiento.entity.Hito;
import pe.contrataia.shared.enums.EstadoProyecto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "proyectos")
@Getter
@Setter
@NoArgsConstructor
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entidad_publica_id", nullable = false)
    private EntidadPublica entidadPublica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_adjudicada_id")
    private Empresa empresaAdjudicada;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(precision = 15, scale = 2)
    private BigDecimal presupuesto;

    private String rubro;
    private String distrito;
    private String provincia;
    private String region;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    private LocalDate fechaInicioPrevista;
    private LocalDate fechaFinPrevista;
    private Integer plazoMeses;

    @Column(columnDefinition = "TEXT")
    private String requisitos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoProyecto estado = EstadoProyecto.BORRADOR;

    @Column(unique = true, length = 20)
    private String codigoUnico;

    @Column(precision = 5, scale = 2)
    private BigDecimal avanceFisico = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal avanceFinanciero = BigDecimal.ZERO;

    private LocalDateTime fechaAdjudicacion;

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CandidatoProveedor> candidatos = new ArrayList<>();

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<Hito> hitos = new ArrayList<>();

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("fechaRegistro DESC")
    private List<Avance> avances = new ArrayList<>();

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Evidencia> evidencias = new ArrayList<>();

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Documento> documentos = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
