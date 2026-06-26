package pe.contrataia.seguimiento.dto;

import pe.contrataia.shared.enums.EstadoHito;

import java.time.LocalDate;
import java.util.UUID;

public record HitoResponse(
        UUID id,
        String nombre,
        String descripcion,
        LocalDate fechaPrevista,
        LocalDate fechaReal,
        EstadoHito estado,
        Integer orden
) {}
