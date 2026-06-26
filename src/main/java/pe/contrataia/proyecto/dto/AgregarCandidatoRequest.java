package pe.contrataia.proyecto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgregarCandidatoRequest(
        @NotBlank @Size(min = 11, max = 11, message = "El RUC debe tener 11 dígitos") String ruc
) {}
