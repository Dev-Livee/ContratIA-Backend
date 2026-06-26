package pe.contrataia.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import pe.contrataia.auth.entity.OtpToken;
import pe.contrataia.shared.enums.TipoOtp;

import java.util.Optional;
import java.util.UUID;

public interface OtpTokenRepository extends JpaRepository<OtpToken, UUID> {

    Optional<OtpToken> findTopByEmailAndTipoAndUsadoFalseOrderByCreatedAtDesc(String email, TipoOtp tipo);

    @Modifying
    @Query("UPDATE OtpToken o SET o.usado = true WHERE o.email = :email AND o.tipo = :tipo")
    void invalidarTodosPorEmailYTipo(String email, TipoOtp tipo);
}
