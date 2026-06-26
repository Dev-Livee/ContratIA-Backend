package pe.contrataia.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.contrataia.empresa.entity.Certificacion;
import pe.contrataia.empresa.entity.DocumentoEmpresa;
import pe.contrataia.empresa.entity.ExperienciaEmpresa;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "empresas")
@DiscriminatorValue("EMPRESA")
@Getter
@Setter
@NoArgsConstructor
public class Empresa extends Usuario {

    @Column(unique = true, nullable = false, length = 11)
    private String ruc;

    @Column(nullable = false)
    private String razonSocial;

    private String estadoSunat;
    private String condicion;
    private String sector;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    private String telefono;
    private String sitioWeb;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String representanteLegal;

    @Column(length = 8)
    private String dniRepresentante;

    private LocalDate fechaInscripcion;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ExperienciaEmpresa> experiencias = new ArrayList<>();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Certificacion> certificaciones = new ArrayList<>();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<DocumentoEmpresa> documentos = new ArrayList<>();
}
