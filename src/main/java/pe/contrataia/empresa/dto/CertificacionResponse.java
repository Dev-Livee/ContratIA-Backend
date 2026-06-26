package pe.contrataia.empresa.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CertificacionResponse(
        UUID id,
        String nombre,
        String entidadEmisora,
        LocalDate fechaEmision,
        LocalDate fechaVencimiento,
        String documentoUrl
) {}
