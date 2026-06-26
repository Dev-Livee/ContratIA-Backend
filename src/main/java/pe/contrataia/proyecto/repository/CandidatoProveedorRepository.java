package pe.contrataia.proyecto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.contrataia.proyecto.entity.CandidatoProveedor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CandidatoProveedorRepository extends JpaRepository<CandidatoProveedor, UUID> {
    List<CandidatoProveedor> findByProyectoIdOrderByFechaAgregadoAsc(UUID proyectoId);
    Optional<CandidatoProveedor> findByProyectoIdAndRuc(UUID proyectoId, String ruc);
    boolean existsByProyectoIdAndRuc(UUID proyectoId, String ruc);
    List<CandidatoProveedor> findByEmpresaIdOrderByFechaAgregadoDesc(UUID empresaId);
    List<CandidatoProveedor> findByRucOrderByFechaAgregadoDesc(String ruc);
}
