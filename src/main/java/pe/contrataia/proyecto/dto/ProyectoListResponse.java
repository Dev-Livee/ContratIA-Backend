package pe.contrataia.proyecto.dto;

import pe.contrataia.shared.enums.EstadoProyecto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProyectoListResponse(
        UUID id,
        String titulo,
        String rubro,
        String distrito,
        String region,
        BigDecimal presupuesto,
        EstadoProyecto estado,
        String codigoUnico,
        BigDecimal avanceFisico,
        LocalDateTime createdAt
) {}
