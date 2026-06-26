package pe.contrataia.publico.dto;

import java.time.LocalDateTime;

public record TimelineEventoResponse(
        String tipo,
        String descripcion,
        LocalDateTime fecha,
        String detalle
) {}
