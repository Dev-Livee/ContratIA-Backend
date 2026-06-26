package pe.contrataia.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "entidades_publicas")
@DiscriminatorValue("ENTIDAD_PUBLICA")
@Getter
@Setter
@NoArgsConstructor
public class EntidadPublica extends Usuario {

    @Column(unique = true, nullable = false, length = 11)
    private String ruc;

    @Column(nullable = false)
    private String razonSocial;

    private String tipo;
    private String distrito;
    private String provincia;
    private String region;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    private String telefono;
    private String representanteLegal;

    @Column(length = 8)
    private String dniRepresentante;

    private String cargo;
}
