package pe.contrataia.empresa.dto;

import java.time.LocalDate;
import java.util.UUID;

public record EmpresaPerfilResponse(
        UUID id,
        String ruc,
        String razonSocial,
        String estadoSunat,
        String condicion,
        String sector,
        String direccion,
        String telefono,
        String sitioWeb,
        String descripcion,
        String representanteLegal,
        String dniRepresentante,
        LocalDate fechaInscripcion,
        String email
) {}
