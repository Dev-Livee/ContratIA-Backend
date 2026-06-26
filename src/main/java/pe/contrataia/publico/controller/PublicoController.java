package pe.contrataia.publico.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.contrataia.publico.dto.ObraDetalleResponse;
import pe.contrataia.publico.dto.ObraPublicaResponse;
import pe.contrataia.publico.dto.TimelineEventoResponse;
import pe.contrataia.publico.service.PublicoService;
import pe.contrataia.shared.enums.EstadoProyecto;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/publico")
@RequiredArgsConstructor
@Tag(name = "Portal Ciudadano", description = "Consulta pública de obras y contratos sin autenticación")
public class PublicoController {

    private final PublicoService publicoService;

    @Operation(summary = "Buscar obras públicas", description = "Búsqueda paginada de proyectos públicos con filtros por distrito, estado, rubro y rango de presupuesto.")
    @GetMapping("/obras")
    public ResponseEntity<Page<ObraPublicaResponse>> buscarObras(
            @RequestParam(required = false) String distrito,
            @RequestParam(required = false) EstadoProyecto estado,
            @RequestParam(required = false) String rubro,
            @RequestParam(required = false) BigDecimal presupuestoMin,
            @RequestParam(required = false) BigDecimal presupuestoMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(publicoService.buscarObras(distrito, estado, rubro, presupuestoMin, presupuestoMax, page, size));
    }

    @Operation(summary = "Detalle de obra pública", description = "Retorna el detalle completo de una obra pública por su código único (generado al crear el proyecto).")
    @GetMapping("/obras/{codigoUnico}")
    public ResponseEntity<ObraDetalleResponse> obtenerDetalle(@PathVariable String codigoUnico) {
        return ResponseEntity.ok(publicoService.obtenerDetalle(codigoUnico));
    }

    @Operation(summary = "Timeline de la obra", description = "Retorna el historial cronológico de eventos (hitos, avances, incidencias) de una obra pública.")
    @GetMapping("/obras/{codigoUnico}/timeline")
    public ResponseEntity<List<TimelineEventoResponse>> obtenerTimeline(@PathVariable String codigoUnico) {
        return ResponseEntity.ok(publicoService.obtenerTimeline(codigoUnico));
    }

    @Operation(summary = "Listar distritos", description = "Retorna la lista de distritos con proyectos registrados, útil para popular filtros en el frontend.")
    @GetMapping("/distritos")
    public ResponseEntity<List<String>> listarDistritos() {
        return ResponseEntity.ok(publicoService.listarDistritos());
    }
}
