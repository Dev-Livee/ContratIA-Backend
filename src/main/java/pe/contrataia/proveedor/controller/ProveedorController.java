package pe.contrataia.proveedor.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.contrataia.proveedor.service.LatInfoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/proveedores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ENTIDAD_PUBLICA')")
@Tag(name = "Proveedores", description = "Consulta de empresas proveedoras via SUNAT, OSCE, OEFA y SEACE (solo ENTIDAD_PUBLICA)")
@SecurityRequirement(name = "Bearer Authentication")
public class ProveedorController {

    private final LatInfoService latInfoService;

    @Operation(summary = "Buscar proveedor por nombre (SUNAT)", description = "Busca empresas en SUNAT por nombre o razón social. Mínimo 3 caracteres, retorna hasta 20 resultados.")
    @GetMapping("/buscar")
    public ResponseEntity<List<Map<String, Object>>> buscar(@RequestParam String q) {
        return ResponseEntity.ok(latInfoService.buscarPorNombre(q));
    }

    @Operation(summary = "Búsqueda agregada de proveedor", description = "Busca en todas las fuentes LatInfo (SUNAT + OSCE + OEFA) y consolida los resultados.")
    @GetMapping("/buscar/agregado")
    public ResponseEntity<List<Map<String, Object>>> buscarAgregado(@RequestParam String q) {
        return ResponseEntity.ok(latInfoService.buscarAgregado(q));
    }

    @Operation(summary = "Ficha KYB completa del proveedor", description = "Retorna la ficha Know Your Business completa: datos SUNAT, habilitación OSCE, sanciones OEFA y licitaciones SEACE.")
    @GetMapping("/{ruc}")
    public ResponseEntity<Map<String, Object>> obtenerFicha(@PathVariable String ruc) {
        return ResponseEntity.ok(latInfoService.obtenerFichaCompleta(ruc));
    }

    @Operation(summary = "Consultar RNP (OSCE)", description = "Retorna la habilitación del proveedor en el Registro Nacional de Proveedores y sus rubros de especialización.")
    @GetMapping("/{ruc}/rnp")
    public ResponseEntity<Map<String, Object>> obtenerRnp(@PathVariable String ruc) {
        return ResponseEntity.ok(latInfoService.obtenerRnp(ruc));
    }

    @Operation(summary = "Consultar deuda coactiva SUNAT", description = "Retorna si el proveedor tiene deuda coactiva activa en SUNAT.")
    @GetMapping("/{ruc}/coactiva")
    public ResponseEntity<Map<String, Object>> obtenerCoactiva(@PathVariable String ruc) {
        return ResponseEntity.ok(latInfoService.obtenerCoactiva(ruc));
    }

    @Operation(summary = "Buscar licitaciones en SEACE", description = "Busca procesos de selección en el SEACE. Filtra por término de búsqueda (`q`) o por entidad compradora (`buyer`).")
    @GetMapping("/licitaciones")
    public ResponseEntity<Map<String, Object>> buscarLicitaciones(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String buyer,
            @RequestParam(defaultValue = "20") Integer limit
    ) {
        return ResponseEntity.ok(latInfoService.buscarLicitaciones(q, buyer, limit));
    }
}
