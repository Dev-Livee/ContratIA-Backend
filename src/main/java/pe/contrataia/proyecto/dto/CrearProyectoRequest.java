package pe.contrataia.proyecto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CrearProyectoRequest(
        @NotBlank String titulo,
        String descripcion,
        @Positive BigDecimal presupuesto,
        @NotBlank String rubro,
        @NotBlank String distrito,
        String provincia,
        String region,
        String direccion,
        LocalDate fechaInicioPrevista,
        LocalDate fechaFinPrevista,
        Integer plazoMeses,
        String requisitos
) {}
