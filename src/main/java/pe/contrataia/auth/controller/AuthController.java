package pe.contrataia.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.contrataia.auth.dto.*;
import pe.contrataia.auth.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro, verificación OTP, login y gestión de tokens")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registrar entidad pública", description = "Crea una cuenta para una entidad pública del Estado. Se envía un OTP al correo para verificar la cuenta.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registro exitoso. Revisar correo para OTP."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o RUC/email ya registrado")
    })
    @PostMapping("/register/entidad")
    public ResponseEntity<Map<String, String>> registrarEntidad(@Valid @RequestBody RegisterEntidadRequest req) {
        authService.registrarEntidad(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Registro exitoso. Revisa tu correo para verificar tu cuenta."));
    }

    @Operation(summary = "Registrar empresa proveedora", description = "Crea una cuenta para una empresa que desea postular a contratos públicos. Se envía un OTP al correo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registro exitoso. Revisar correo para OTP."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o RUC/email ya registrado")
    })
    @PostMapping("/register/empresa")
    public ResponseEntity<Map<String, String>> registrarEmpresa(@Valid @RequestBody RegisterEmpresaRequest req) {
        authService.registrarEmpresa(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Registro exitoso. Revisa tu correo para verificar tu cuenta."));
    }

    @Operation(summary = "Verificar OTP", description = "Verifica el código OTP enviado al correo electrónico para activar la cuenta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correo verificado correctamente"),
            @ApiResponse(responseCode = "400", description = "OTP inválido o expirado")
    })
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verificarOtp(@Valid @RequestBody VerifyOtpRequest req) {
        authService.verificarOtp(req);
        return ResponseEntity.ok(Map.of("message", "Correo verificado correctamente."));
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario y devuelve un access token JWT y un refresh token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas o cuenta no verificada")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @Operation(summary = "Refrescar token", description = "Genera un nuevo access token usando el refresh token válido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token renovado exitosamente"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado")
    })
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return ResponseEntity.ok(authService.refresh(req));
    }

    @Operation(summary = "Cerrar sesión", description = "Invalida el refresh token del usuario.")
    @ApiResponse(responseCode = "200", description = "Sesión cerrada correctamente")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody RefreshTokenRequest req) {
        authService.logout(req);
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente."));
    }
}
