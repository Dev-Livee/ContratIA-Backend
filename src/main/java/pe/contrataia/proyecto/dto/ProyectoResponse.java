package pe.contrataia.proyecto.dto;

import pe.contrataia.shared.enums.EstadoProyecto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProyectoResponse(
        UUID id,
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
        String requisitos,
        EstadoProyecto estado,
        String codigoUnico,
        BigDecimal avanceFisico,
        BigDecimal avanceFinanciero,
        LocalDateTime fechaAdjudicacion,
        EntidadResumen entidad,
        EmpresaResumen empresaAdjudicada,
        LocalDateTime createdAt
) {
    public record EntidadResumen(UUID id, String razonSocial, String ruc, String distrito) {}
    public record EmpresaResumen(UUID id, String razonSocial, String ruc) {}
}
