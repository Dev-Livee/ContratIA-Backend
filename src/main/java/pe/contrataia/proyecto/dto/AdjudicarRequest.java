package pe.contrataia.proyecto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdjudicarRequest(
        @NotBlank @Size(min = 11, max = 11) String rucEmpresaGanadora
) {}
