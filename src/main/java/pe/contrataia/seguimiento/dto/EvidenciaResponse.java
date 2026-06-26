package pe.contrataia.seguimiento.dto;

import pe.contrataia.shared.enums.TipoEvidencia;

import java.time.LocalDateTime;
import java.util.UUID;

public record EvidenciaResponse(
        UUID id,
        TipoEvidencia tipo,
        String nombre,
        String url,
        String descripcion,
        boolean esPublico,
        UUID hitoId,
        LocalDateTime fechaSubida
) {}
