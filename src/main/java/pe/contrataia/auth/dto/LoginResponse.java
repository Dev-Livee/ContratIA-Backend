package pe.contrataia.auth.dto;

import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UUID userId,
        String email,
        String role,
        String razonSocial
) {
    public static LoginResponse of(String access, String refresh, UUID id, String email, String role, String razonSocial) {
        return new LoginResponse(access, refresh, "Bearer", id, email, role, razonSocial);
    }
}
