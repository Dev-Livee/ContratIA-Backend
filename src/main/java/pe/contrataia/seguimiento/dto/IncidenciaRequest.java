package pe.contrataia.seguimiento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pe.contrataia.shared.enums.SeveridadIncidencia;
import pe.contrataia.shared.enums.TipoIncidencia;

import java.util.UUID;

public record IncidenciaRequest(
        @NotBlank String titulo,
        @NotBlank String descripcion,
        @NotNull TipoIncidencia tipo,
        @NotNull SeveridadIncidencia severidad,
        UUID hitoId
) {}
