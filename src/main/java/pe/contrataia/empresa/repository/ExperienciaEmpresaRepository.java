package pe.contrataia.empresa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.contrataia.empresa.entity.ExperienciaEmpresa;

import java.util.List;
import java.util.UUID;

public interface ExperienciaEmpresaRepository extends JpaRepository<ExperienciaEmpresa, UUID> {
    List<ExperienciaEmpresa> findByEmpresaIdOrderByFechaInicioDesc(UUID empresaId);
}
