package pe.contrataia.seguimiento.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AvanceResponse(
        UUID id,
        BigDecimal avanceFisico,
        BigDecimal avanceFinanciero,
        String descripcion,
        LocalDateTime fechaRegistro
) {}
