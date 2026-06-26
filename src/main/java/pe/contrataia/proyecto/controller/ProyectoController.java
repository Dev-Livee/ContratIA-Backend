package pe.contrataia.proyecto.controller;

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
import pe.contrataia.proyecto.dto.*;
import pe.contrataia.proyecto.service.ProyectoService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/proyectos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ENTIDAD_PUBLICA')")
@Tag(name = "Proyectos", description = "Gestión de proyectos de contratación pública (solo ENTIDAD_PUBLICA)")
@SecurityRequirement(name = "Bearer Authentication")
public class ProyectoController {

    private final ProyectoService proyectoService;

    @Operation(summary = "Crear proyecto", description = "Crea un nuevo proyecto de contratación pública para la entidad autenticada.")
    @PostMapping
    public ResponseEntity<ProyectoResponse> crear(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody CrearProyectoRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(proyectoService.crear(user.getUsername(), req));
    }

    @Operation(summary = "Listar proyectos", description = "Retorna todos los proyectos de la entidad autenticada.")
    @GetMapping
    public ResponseEntity<List<ProyectoListResponse>> listar(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(proyectoService.listarMisProyectos(user.getUsername()));
    }

    @Operation(summary = "Obtener proyecto", description = "Retorna el detalle completo de un proyecto por ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ProyectoResponse> obtener(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(proyectoService.obtener(user.getUsername(), id));
    }

    @Operation(summary = "Actualizar proyecto", description = "Actualiza los datos de un proyecto existente.")
    @PutMapping("/{id}")
    public ResponseEntity<ProyectoResponse> actualizar(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID id,
            @RequestBody ActualizarProyectoRequest req
    ) {
        return ResponseEntity.ok(proyectoService.actualizar(user.getUsername(), id, req));
    }

    @Operation(summary = "Agregar candidato", description = "Agrega una empresa candidata (por RUC) al proceso de selección del proyecto.")
    @PostMapping("/{id}/candidatos")
    public ResponseEntity<CandidatoResponse> agregarCandidato(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID id,
            @Valid @RequestBody AgregarCandidatoRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(proyectoService.agregarCandidato(user.getUsername(), id, req));
    }

    @Operation(summary = "Listar candidatos", description = "Retorna la lista de empresas candidatas del proyecto con su evaluación IA.")
    @GetMapping("/{id}/candidatos")
    public ResponseEntity<List<CandidatoResponse>> listarCandidatos(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(proyectoService.listarCandidatos(user.getUsername(), id));
    }

    @Operation(summary = "Eliminar candidato", description = "Elimina una empresa del proceso de selección del proyecto.")
    @DeleteMapping("/{id}/candidatos/{candidatoId}")
    public ResponseEntity<Void> eliminarCandidato(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID id,
            @PathVariable UUID candidatoId
    ) {
        proyectoService.eliminarCandidato(user.getUsername(), id, candidatoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Comparador de candidatos", description = "Retorna los candidatos con scores IA para comparación y toma de decisión.")
    @GetMapping("/{id}/comparador")
    public ResponseEntity<List<CandidatoResponse>> comparador(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(proyectoService.obtenerComparador(user.getUsername(), id));
    }

    @Operation(summary = "Adjudicar proyecto", description = "Adjudica el contrato a un candidato ganador y cierra el proceso de selección.")
    @PostMapping("/{id}/adjudicar")
    public ResponseEntity<ProyectoResponse> adjudicar(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID id,
            @Valid @RequestBody AdjudicarRequest req
    ) {
        return ResponseEntity.ok(proyectoService.adjudicar(user.getUsername(), id, req));
    }

    @Operation(summary = "Actualizar estado del proyecto", description = "Cambia el estado del proyecto (EN_PROCESO, COMPLETADO, CANCELADO, etc.).")
    @PutMapping("/{id}/estado")
    public ResponseEntity<ProyectoResponse> actualizarEstado(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarEstadoRequest req
    ) {
        return ResponseEntity.ok(proyectoService.actualizarEstado(user.getUsername(), id, req));
    }
}
