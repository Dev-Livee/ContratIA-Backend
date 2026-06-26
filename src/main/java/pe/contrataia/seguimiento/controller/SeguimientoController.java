package pe.contrataia.seguimiento.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.contrataia.seguimiento.dto.*;
import pe.contrataia.seguimiento.service.SeguimientoService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/proyectos/{proyectoId}")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ENTIDAD_PUBLICA')")
@Tag(name = "Seguimiento", description = "Hitos, avances, evidencias, documentos e incidencias de un proyecto en ejecución (solo ENTIDAD_PUBLICA)")
@SecurityRequirement(name = "Bearer Authentication")
public class SeguimientoController {

    private final SeguimientoService seguimientoService;

    @Operation(summary = "Crear hito", description = "Define un hito o entregable clave del proyecto con fecha límite.")
    @PostMapping("/hitos")
    public ResponseEntity<HitoResponse> crearHito(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID proyectoId,
            @Valid @RequestBody HitoRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(seguimientoService.crearHito(user.getUsername(), proyectoId, req));
    }

    @Operation(summary = "Listar hitos", description = "Retorna todos los hitos del proyecto con su estado de cumplimiento.")
    @GetMapping("/hitos")
    public ResponseEntity<List<HitoResponse>> listarHitos(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID proyectoId
    ) {
        return ResponseEntity.ok(seguimientoService.listarHitos(user.getUsername(), proyectoId));
    }

    @Operation(summary = "Actualizar hito", description = "Modifica los datos o marca como completado un hito del proyecto.")
    @PutMapping("/hitos/{hitoId}")
    public ResponseEntity<HitoResponse> actualizarHito(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID proyectoId,
            @PathVariable UUID hitoId,
            @RequestBody HitoRequest req
    ) {
        return ResponseEntity.ok(seguimientoService.actualizarHito(user.getUsername(), proyectoId, hitoId, req));
    }

    @Operation(summary = "Registrar avance", description = "Registra el porcentaje de avance físico o financiero del proyecto.")
    @PostMapping("/avances")
    public ResponseEntity<AvanceResponse> registrarAvance(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID proyectoId,
            @Valid @RequestBody AvanceRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(seguimientoService.registrarAvance(user.getUsername(), proyectoId, req));
    }

    @Operation(summary = "Listar avances", description = "Retorna el historial de avances registrados en el proyecto.")
    @GetMapping("/avances")
    public ResponseEntity<List<AvanceResponse>> listarAvances(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID proyectoId
    ) {
        return ResponseEntity.ok(seguimientoService.listarAvances(user.getUsername(), proyectoId));
    }

    @Operation(summary = "Agregar evidencia", description = "Adjunta una evidencia fotográfica o documental del avance del proyecto.")
    @PostMapping("/evidencias")
    public ResponseEntity<EvidenciaResponse> agregarEvidencia(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID proyectoId,
            @Valid @RequestBody EvidenciaRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(seguimientoService.agregarEvidencia(user.getUsername(), proyectoId, req));
    }

    @Operation(summary = "Listar evidencias", description = "Retorna todas las evidencias adjuntas al proyecto.")
    @GetMapping("/evidencias")
    public ResponseEntity<List<EvidenciaResponse>> listarEvidencias(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID proyectoId
    ) {
        return ResponseEntity.ok(seguimientoService.listarEvidencias(user.getUsername(), proyectoId));
    }

    @Operation(summary = "Agregar documento al proyecto", description = "Adjunta un documento contractual o técnico al expediente del proyecto.")
    @PostMapping("/documentos")
    public ResponseEntity<DocumentoResponse> agregarDocumento(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID proyectoId,
            @Valid @RequestBody DocumentoRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(seguimientoService.agregarDocumento(user.getUsername(), proyectoId, req));
    }

    @Operation(summary = "Listar documentos del proyecto", description = "Retorna todos los documentos adjuntos al expediente del proyecto.")
    @GetMapping("/documentos")
    public ResponseEntity<List<DocumentoResponse>> listarDocumentos(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID proyectoId
    ) {
        return ResponseEntity.ok(seguimientoService.listarDocumentos(user.getUsername(), proyectoId));
    }

    @Operation(summary = "Reportar incidencia", description = "Registra una incidencia, alerta o problema detectado durante la ejecución del proyecto.")
    @PostMapping("/incidencias")
    public ResponseEntity<IncidenciaResponse> reportarIncidencia(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID proyectoId,
            @Valid @RequestBody IncidenciaRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(seguimientoService.reportarIncidencia(user.getUsername(), proyectoId, req));
    }

    @Operation(summary = "Listar incidencias", description = "Retorna todas las incidencias reportadas en el proyecto.")
    @GetMapping("/incidencias")
    public ResponseEntity<List<IncidenciaResponse>> listarIncidencias(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID proyectoId
    ) {
        return ResponseEntity.ok(seguimientoService.listarIncidencias(user.getUsername(), proyectoId));
    }

    @Operation(summary = "Resolver incidencia", description = "Marca una incidencia como resuelta e ingresa la acción correctiva tomada.")
    @PatchMapping("/incidencias/{incidenciaId}/resolver")
    public ResponseEntity<IncidenciaResponse> resolverIncidencia(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID proyectoId,
            @PathVariable UUID incidenciaId,
            @Valid @RequestBody ResolverIncidenciaRequest req
    ) {
        return ResponseEntity.ok(seguimientoService.resolverIncidencia(user.getUsername(), proyectoId, incidenciaId, req));
    }
}
