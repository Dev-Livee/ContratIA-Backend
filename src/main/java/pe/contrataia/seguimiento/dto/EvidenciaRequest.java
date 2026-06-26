package pe.contrataia.seguimiento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pe.contrataia.shared.enums.TipoEvidencia;

import java.util.UUID;

public record EvidenciaRequest(
        @NotNull TipoEvidencia tipo,
        String nombre,
        @NotBlank String url,
        String descripcion,
        boolean esPublico,
        UUID hitoId
) {}
