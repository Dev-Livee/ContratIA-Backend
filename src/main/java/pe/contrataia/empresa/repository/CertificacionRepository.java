package pe.contrataia.empresa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.contrataia.empresa.entity.Certificacion;

import java.util.List;
import java.util.UUID;

public interface CertificacionRepository extends JpaRepository<Certificacion, UUID> {
    List<Certificacion> findByEmpresaIdOrderByFechaEmisionDesc(UUID empresaId);
}
