package pe.contrataia.auth.dto;

import jakarta.validation.constraints.*;

public record RegisterEntidadRequest(
        @NotBlank @Size(min = 11, max = 11, message = "El RUC debe tener 11 dígitos") String ruc,
        @NotBlank String razonSocial,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 8, message = "El DNI debe tener 8 dígitos") String dniRepresentante,
        @NotBlank String representanteLegal,
        @NotBlank @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String password,
        String tipo,
        String distrito,
        String provincia,
        String region,
        String telefono,
        String cargo
) {}
