package pe.contrataia.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import pe.contrataia.auth.entity.RefreshToken;
import pe.contrataia.auth.entity.Usuario;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revocado = true WHERE r.usuario = :usuario")
    void revocarTodosPorUsuario(Usuario usuario);
}
