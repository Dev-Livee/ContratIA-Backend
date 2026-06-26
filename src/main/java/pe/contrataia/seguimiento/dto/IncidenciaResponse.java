package pe.contrataia.seguimiento.dto;

import pe.contrataia.shared.enums.EstadoIncidencia;
import pe.contrataia.shared.enums.SeveridadIncidencia;
import pe.contrataia.shared.enums.TipoIncidencia;

import java.time.LocalDateTime;
import java.util.UUID;

public record IncidenciaResponse(
        UUID id,
        String titulo,
        String descripcion,
        TipoIncidencia tipo,
        SeveridadIncidencia severidad,
        EstadoIncidencia estado,
        UUID hitoId,
        LocalDateTime fechaReporte,
        LocalDateTime fechaResolucion,
        String resolucion
) {}
