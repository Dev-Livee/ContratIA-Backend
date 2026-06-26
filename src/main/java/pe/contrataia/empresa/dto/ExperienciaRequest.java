package pe.contrataia.empresa.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExperienciaRequest(
        @NotBlank String descripcion,
        String entidadContratante,
        BigDecimal monto,
        String rubro,
        String region,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {}
