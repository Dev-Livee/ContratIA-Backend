package pe.contrataia.empresa.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExperienciaResponse(
        UUID id,
        String descripcion,
        String entidadContratante,
        BigDecimal monto,
        String rubro,
        String region,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {}
