package pe.contrataia.empresa.dto;

import pe.contrataia.shared.enums.EstadoCandidato;
import pe.contrataia.shared.enums.EstadoProyecto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EvaluacionHistoricaResponse(
        UUID proyectoId,
        String codigoUnico,
        String tituloProyecto,
        String rubro,
        String distrito,
        BigDecimal presupuesto,
        EstadoProyecto estadoProyecto,
        EstadoCandidato resultadoEvaluacion,
        LocalDateTime fechaEvaluacion,
        String entidadNombre
) {}
