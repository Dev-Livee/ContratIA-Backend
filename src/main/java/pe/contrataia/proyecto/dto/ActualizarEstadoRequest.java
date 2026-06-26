package pe.contrataia.proyecto.dto;

import jakarta.validation.constraints.NotNull;
import pe.contrataia.shared.enums.EstadoProyecto;

public record ActualizarEstadoRequest(
        @NotNull EstadoProyecto estado
) {}
