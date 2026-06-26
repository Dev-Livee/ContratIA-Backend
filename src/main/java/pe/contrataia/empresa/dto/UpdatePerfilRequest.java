package pe.contrataia.empresa.dto;

public record UpdatePerfilRequest(
        String sector,
        String direccion,
        String telefono,
        String sitioWeb,
        String descripcion,
        String representanteLegal
) {}
