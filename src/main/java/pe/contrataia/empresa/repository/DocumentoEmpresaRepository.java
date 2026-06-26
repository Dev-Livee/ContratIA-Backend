package pe.contrataia.empresa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.contrataia.empresa.entity.DocumentoEmpresa;

import java.util.List;
import java.util.UUID;

public interface DocumentoEmpresaRepository extends JpaRepository<DocumentoEmpresa, UUID> {
    List<DocumentoEmpresa> findByEmpresaIdOrderByFechaSubidaDesc(UUID empresaId);
}
