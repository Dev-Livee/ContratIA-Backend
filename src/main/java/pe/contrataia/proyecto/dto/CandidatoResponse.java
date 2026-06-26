package pe.contrataia.proyecto.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import pe.contrataia.shared.enums.EstadoCandidato;

import java.time.LocalDateTime;
import java.util.UUID;

public record CandidatoResponse(
        UUID id,
        String ruc,
        String razonSocial,
        EstadoCandidato estado,
        @JsonRawValue String snapshotLatinfo,
        LocalDateTime fechaAgregado
) {}
