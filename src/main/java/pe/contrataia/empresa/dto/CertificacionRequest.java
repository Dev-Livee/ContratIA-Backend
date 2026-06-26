package pe.contrataia.empresa.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CertificacionRequest(
        @NotBlank String nombre,
        String entidadEmisora,
        LocalDate fechaEmision,
        LocalDate fechaVencimiento,
        String documentoUrl
) {}
