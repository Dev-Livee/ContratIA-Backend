package pe.contrataia.seguimiento.dto;

import pe.contrataia.shared.enums.TipoDocumento;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentoResponse(
        UUID id,
        TipoDocumento tipo,
        String nombre,
        String url,
        Integer version,
        boolean esPublico,
        LocalDateTime fechaSubida
) {}
