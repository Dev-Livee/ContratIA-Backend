package pe.contrataia.empresa.controller;

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
import pe.contrataia.empresa.dto.*;
import pe.contrataia.empresa.service.EmpresaService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/empresa")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPRESA')")
@Tag(name = "Empresa", description = "Gestión del perfil, experiencia, certificaciones y documentos de la empresa (solo EMPRESA)")
@SecurityRequirement(name = "Bearer Authentication")
public class EmpresaController {

    private final EmpresaService empresaService;

    @Operation(summary = "Obtener perfil de empresa", description = "Retorna el perfil completo de la empresa autenticada.")
    @GetMapping("/perfil")
    public ResponseEntity<EmpresaPerfilResponse> obtenerPerfil(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empresaService.obtenerPerfil(user.getUsername()));
    }

    @Operation(summary = "Actualizar perfil de empresa", description = "Actualiza los datos del perfil de la empresa (descripción, sector, capacidad, etc.).")
    @PutMapping("/perfil")
    public ResponseEntity<EmpresaPerfilResponse> actualizarPerfil(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody UpdatePerfilRequest req
    ) {
        return ResponseEntity.ok(empresaService.actualizarPerfil(user.getUsername(), req));
    }

    @Operation(summary = "Agregar experiencia", description = "Registra un proyecto o contrato previo como experiencia de la empresa.")
    @PostMapping("/experiencia")
    public ResponseEntity<ExperienciaResponse> agregarExperiencia(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody ExperienciaRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(empresaService.agregarExperiencia(user.getUsername(), req));
    }

    @Operation(summary = "Listar experiencias", description = "Retorna todas las experiencias registradas de la empresa.")
    @GetMapping("/experiencia")
    public ResponseEntity<List<ExperienciaResponse>> listarExperiencias(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empresaService.listarExperiencias(user.getUsername()));
    }

    @Operation(summary = "Eliminar experiencia", description = "Elimina una experiencia de la empresa por ID.")
    @DeleteMapping("/experiencia/{id}")
    public ResponseEntity<Void> eliminarExperiencia(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID id
    ) {
        empresaService.eliminarExperiencia(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Agregar certificación", description = "Registra una certificación o norma de calidad que posee la empresa (ISO, etc.).")
    @PostMapping("/certificaciones")
    public ResponseEntity<CertificacionResponse> agregarCertificacion(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody CertificacionRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(empresaService.agregarCertificacion(user.getUsername(), req));
    }

    @Operation(summary = "Listar certificaciones", description = "Retorna todas las certificaciones de la empresa.")
    @GetMapping("/certificaciones")
    public ResponseEntity<List<CertificacionResponse>> listarCertificaciones(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(empresaService.listarCertificaciones(user.getUsername()));
    }

    @Operation(summary = "Eliminar certificación", description = "Elimina una certificación de la empresa por ID.")
    @DeleteMapping("/certificaciones/{id}")
    public ResponseEntity<Void> eliminarCertificacion(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID id
    ) {
        empresaService.eliminarCertificacion(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Consultar evaluaciones históricas", description = "Retorna el historial de evaluaciones IA recibidas por la empresa en procesos de selección anteriores.")
    @GetMapping("/evaluaciones")
    public ResponseEntity<List<EvaluacionHistoricaResponse>> consultarEvaluaciones(
            @AuthenticationPrincipal UserDetails user
    ) {
        return ResponseEntity.ok(empresaService.consultarEvaluacionesHistoricas(user.getUsername()));
    }

    @Operation(summary = "Agregar documento", description = "Adjunta un documento (RNP, estados financieros, etc.) al perfil de la empresa. Requiere URL del archivo ya subido.")
    @PostMapping("/documentos")
    public ResponseEntity<Map<String, String>> agregarDocumento(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam String tipo,
            @RequestParam String nombre,
            @RequestParam String url
    ) {
        empresaService.agregarDocumento(user.getUsername(), tipo, nombre, url);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Documento agregado correctamente."));
    }
}
