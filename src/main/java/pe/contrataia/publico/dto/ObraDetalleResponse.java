package pe.contrataia.publico.dto;

import pe.contrataia.seguimiento.dto.AvanceResponse;
import pe.contrataia.seguimiento.dto.DocumentoResponse;
import pe.contrataia.seguimiento.dto.EvidenciaResponse;
import pe.contrataia.seguimiento.dto.HitoResponse;
import pe.contrataia.shared.enums.EstadoProyecto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ObraDetalleResponse(
        UUID id,
        String codigoUnico,
        String titulo,
        String descripcion,
        String requisitos,
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
        LocalDateTime fechaAdjudicacion,
        String entidadNombre,
        String entidadTipo,
        String empresaEjecutoraNombre,
        String empresaEjecutoraRuc,
        List<HitoResponse> hitos,
        List<AvanceResponse> avancesRecientes,
        List<EvidenciaResponse> evidenciasPublicas,
        List<DocumentoResponse> documentosPublicos
) {}
