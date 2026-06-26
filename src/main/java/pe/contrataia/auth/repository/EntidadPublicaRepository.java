package pe.contrataia.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.contrataia.auth.entity.EntidadPublica;

import java.util.Optional;
import java.util.UUID;

public interface EntidadPublicaRepository extends JpaRepository<EntidadPublica, UUID> {
    Optional<EntidadPublica> findByEmail(String email);
    boolean existsByRuc(String ruc);
    boolean existsByEmail(String email);
}
