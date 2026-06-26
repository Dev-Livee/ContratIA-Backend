package pe.contrataia.seguimiento.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AvanceRequest(
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal avanceFisico,
        @DecimalMin("0") @DecimalMax("100") BigDecimal avanceFinanciero,
        String descripcion
) {}
