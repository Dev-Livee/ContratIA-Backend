package pe.contrataia.publico.dto;

import pe.contrataia.shared.enums.EstadoProyecto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ObraPublicaResponse(
        UUID id,
        String codigoUnico,
        String titulo,
        String descripcion,
        BigDecimal presupuesto,
        String rubro,
        String distrito,
        String provincia,
        String region,
        String direccion,
        LocalDate fechaInicioPrevista,
        LocalDate fechaFinPrevista,
        Integer plazoMeses,
        EstadoProyecto estado,
        BigDecimal avanceFisico,
        BigDecimal avanceFinanciero,
        String entidadNombre,
        String empresaEjecutoraNombre
) {}
