package pe.contrataia.proveedor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pe.contrataia.shared.exception.BusinessException;
import pe.contrataia.shared.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LatInfoService {

    private final WebClient latInfoWebClient;

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> buscarPorNombre(String query) {
        if (query == null || query.trim().length() < 3) {
            throw new BusinessException("La búsqueda debe tener al menos 3 caracteres");
        }
        try {
            return latInfoWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/pe/sunat/padron/search")
                            .queryParam("q", query.trim())
                            .queryParam("limit", 20)
                            .build())
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
        } catch (Exception e) {
            log.error("Error buscando proveedor en LatInfo: {}", e.getMessage());
            throw new BusinessException("Error consultando datos de proveedor");
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerFichaCompleta(String ruc) {
        validarRuc(ruc);
        try {
            Map<String, Object> result = latInfoWebClient.get()
                    .uri("/pe/sunat/padron/ruc/{ruc}", ruc)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (result == null) {
                throw new ResourceNotFoundException("Proveedor con RUC", ruc);
            }
            return result;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error obteniendo ficha de RUC {}: {}", ruc, e.getMessage());
            throw new BusinessException("Error consultando datos del proveedor con RUC: " + ruc);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerRnp(String ruc) {
        validarRuc(ruc);
        try {
            // LatInfo no expone endpoint RNP dedicado — devuelve los datos del padrón SUNAT
            Map<String, Object> result = latInfoWebClient.get()
                    .uri("/pe/sunat/padron/ruc/{ruc}", ruc)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (result == null) throw new ResourceNotFoundException("RNP para RUC", ruc);
            return result;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error consultando RNP RUC {}: {}", ruc, e.getMessage());
            throw new BusinessException("Error consultando RNP del proveedor");
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerCoactiva(String ruc) {
        validarRuc(ruc);
        try {
            Map<String, Object> result = latInfoWebClient.get()
                    .uri("/pe/sunat/coactiva/ruc/{ruc}", ruc)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return result != null ? result : Map.of();
        } catch (Exception e) {
            log.warn("Sin datos coactivos para RUC {}: {}", ruc, e.getMessage());
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> buscarLicitaciones(String query, String buyerRuc, Integer limit) {
        try {
            return latInfoWebClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/pe/oece/tenders");
                        if (query != null && !query.isBlank()) uriBuilder.queryParam("q", query.trim());
                        if (buyerRuc != null && !buyerRuc.isBlank()) uriBuilder.queryParam("buyer", buyerRuc);
                        uriBuilder.queryParam("limit", limit != null ? limit : 20);
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            log.error("Error buscando licitaciones: {}", e.getMessage());
            throw new BusinessException("Error consultando licitaciones");
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> buscarAgregado(String query) {
        if (query == null || query.trim().length() < 3) {
            throw new BusinessException("La búsqueda debe tener al menos 3 caracteres");
        }
        try {
            return latInfoWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/pe/sunat/padron/search").queryParam("q", query.trim()).build())
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
        } catch (Exception e) {
            log.error("Error en búsqueda agregada: {}", e.getMessage());
            throw new BusinessException("Error en búsqueda de proveedores");
        }
    }

    private void validarRuc(String ruc) {
        if (ruc == null || !ruc.matches("20\\d{9}")) {
            throw new BusinessException("RUC inválido. Solo se aceptan RUC de empresas (inicio 20)");
        }
    }
}
