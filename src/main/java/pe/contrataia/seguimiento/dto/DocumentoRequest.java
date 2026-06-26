package pe.contrataia.seguimiento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pe.contrataia.shared.enums.TipoDocumento;

public record DocumentoRequest(
        @NotNull TipoDocumento tipo,
        @NotBlank String nombre,
        @NotBlank String url,
        boolean esPublico
) {}
