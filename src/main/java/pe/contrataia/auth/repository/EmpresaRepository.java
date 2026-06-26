package pe.contrataia.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.contrataia.auth.entity.Empresa;

import java.util.Optional;
import java.util.UUID;

public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {
    Optional<Empresa> findByEmail(String email);
    Optional<Empresa> findByRuc(String ruc);
    boolean existsByRuc(String ruc);
    boolean existsByEmail(String email);
}
