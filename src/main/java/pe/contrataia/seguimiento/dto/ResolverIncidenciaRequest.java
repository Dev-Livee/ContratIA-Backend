package pe.contrataia.seguimiento.dto;

import jakarta.validation.constraints.NotBlank;

public record ResolverIncidenciaRequest(@NotBlank String resolucion) {}
