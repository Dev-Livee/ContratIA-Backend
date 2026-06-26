package pe.contrataia.proyecto.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ActualizarProyectoRequest(
        String titulo,
        String descripcion,
        BigDecimal presupuesto,
        String rubro,
        String distrito,
        String provincia,
        String region,
        String direccion,
        LocalDate fechaInicioPrevista,
        LocalDate fechaFinPrevista,
        Integer plazoMeses,
        String requisitos
) {}
