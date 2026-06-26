package pe.contrataia.seguimiento.dto;

import jakarta.validation.constraints.NotBlank;
import pe.contrataia.shared.enums.EstadoHito;

import java.time.LocalDate;

public record HitoRequest(
        @NotBlank String nombre,
        String descripcion,
        LocalDate fechaPrevista,
        LocalDate fechaReal,
        EstadoHito estado,
        Integer orden
) {}
