package pe.contrataia.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.contrataia.auth.dto.*;
import pe.contrataia.auth.entity.*;
import pe.contrataia.auth.repository.*;
import pe.contrataia.shared.enums.Role;
import pe.contrataia.shared.enums.TipoOtp;
import pe.contrataia.shared.exception.BusinessException;
import pe.contrataia.shared.security.JwtService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EntidadPublicaRepository entidadRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Transactional
    public void registrarEntidad(RegisterEntidadRequest req) {
        if (usuarioRepository.existsByEmail(req.email())) {
            throw new BusinessException("El correo ya está registrado");
        }
        if (entidadRepository.existsByRuc(req.ruc())) {
            throw new BusinessException("El RUC ya está registrado");
        }

        EntidadPublica entidad = new EntidadPublica();
        entidad.setEmail(req.email());
        entidad.setPassword(passwordEncoder.encode(req.password()));
        entidad.setRole(Role.ENTIDAD_PUBLICA);
        entidad.setRuc(req.ruc());
        entidad.setRazonSocial(req.razonSocial());
        entidad.setRepresentanteLegal(req.representanteLegal());
        entidad.setDniRepresentante(req.dniRepresentante());
        entidad.setTipo(req.tipo());
        entidad.setDistrito(req.distrito());
        entidad.setProvincia(req.provincia());
        entidad.setRegion(req.region());
        entidad.setTelefono(req.telefono());
        entidad.setCargo(req.cargo());
        entidad.setEmailVerificado(true);
        entidadRepository.save(entidad);
    }

    @Transactional
    public void registrarEmpresa(RegisterEmpresaRequest req) {
        if (usuarioRepository.existsByEmail(req.email())) {
            throw new BusinessException("El correo ya está registrado");
        }
        if (empresaRepository.existsByRuc(req.ruc())) {
            throw new BusinessException("El RUC ya está registrado");
        }

        Empresa empresa = new Empresa();
        empresa.setEmail(req.email());
        empresa.setPassword(passwordEncoder.encode(req.password()));
        empresa.setRole(Role.EMPRESA);
        empresa.setRuc(req.ruc());
        empresa.setRazonSocial(req.razonSocial());
        empresa.setRepresentanteLegal(req.representanteLegal());
        empresa.setDniRepresentante(req.dniRepresentante());
        empresa.setSector(req.sector());
        empresa.setTelefono(req.telefono());
        empresa.setSitioWeb(req.sitioWeb());
        empresa.setDescripcion(req.descripcion());
        empresa.setEmailVerificado(true);
        empresaRepository.save(empresa);
    }

    @Transactional
    public void verificarOtp(VerifyOtpRequest req) {
        otpService.verificar(req.email(), req.codigo(), TipoOtp.REGISTRO);

        Usuario usuario = usuarioRepository.findByEmail(req.email())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado", HttpStatus.NOT_FOUND));
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public LoginResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        Usuario usuario = usuarioRepository.findByEmail(req.email())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado", HttpStatus.NOT_FOUND));

        UserDetails userDetails = userDetailsService.loadUserByUsername(req.email());
        Map<String, Object> claims = Map.of("role", usuario.getRole().name(), "userId", usuario.getId().toString());

        String accessToken = jwtService.generateToken(userDetails, claims);
        String refreshToken = crearRefreshToken(usuario);

        String razonSocial = switch (usuario.getRole()) {
            case ENTIDAD_PUBLICA -> entidadRepository.findByEmail(req.email())
                    .map(EntidadPublica::getRazonSocial).orElse("");
            case EMPRESA -> empresaRepository.findByEmail(req.email())
                    .map(Empresa::getRazonSocial).orElse("");
        };

        return LoginResponse.of(accessToken, refreshToken, usuario.getId(), usuario.getEmail(),
                usuario.getRole().name(), razonSocial);
    }

    @Transactional
    public LoginResponse refresh(RefreshTokenRequest req) {
        RefreshToken stored = refreshTokenRepository.findByToken(req.refreshToken())
                .orElseThrow(() -> new BusinessException("Refresh token inválido", HttpStatus.UNAUTHORIZED));

        if (stored.isRevocado() || stored.isExpired()) {
            throw new BusinessException("Refresh token expirado o revocado", HttpStatus.UNAUTHORIZED);
        }

        Usuario usuario = stored.getUsuario();
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        Map<String, Object> claims = Map.of("role", usuario.getRole().name(), "userId", usuario.getId().toString());
        String newAccessToken = jwtService.generateToken(userDetails, claims);

        // Rotar refresh token
        stored.setRevocado(true);
        refreshTokenRepository.save(stored);
        String newRefreshToken = crearRefreshToken(usuario);

        String razonSocial = switch (usuario.getRole()) {
            case ENTIDAD_PUBLICA -> entidadRepository.findById(usuario.getId())
                    .map(EntidadPublica::getRazonSocial).orElse("");
            case EMPRESA -> empresaRepository.findById(usuario.getId())
                    .map(Empresa::getRazonSocial).orElse("");
        };

        return LoginResponse.of(newAccessToken, newRefreshToken, usuario.getId(), usuario.getEmail(),
                usuario.getRole().name(), razonSocial);
    }

    @Transactional
    public void logout(RefreshTokenRequest req) {
        refreshTokenRepository.findByToken(req.refreshToken()).ifPresent(token -> {
            token.setRevocado(true);
            refreshTokenRepository.save(token);
        });
    }

    private String crearRefreshToken(Usuario usuario) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsuario(usuario);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000L));
        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }
}
