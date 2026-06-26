package pe.contrataia.proveedor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProveedorFichaResponse(
        String ruc,
        String razon_social,
        String estado,
        String condicion,
        String ubigeo,
        String nombre_via,
        String numero,
        Map<String, Object> osce_sanctions,
        Map<String, Object> osce_fines,
        Map<String, Object> osce_penalidades,
        Map<String, Object> oefa_sanctions,
        Map<String, Object> seace_tenders
) {}
