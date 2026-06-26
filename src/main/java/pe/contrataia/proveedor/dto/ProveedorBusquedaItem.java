package pe.contrataia.proveedor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProveedorBusquedaItem(
        String ruc,
        String razon_social,
        String estado,
        String condicion,
        String ubigeo
) {}
