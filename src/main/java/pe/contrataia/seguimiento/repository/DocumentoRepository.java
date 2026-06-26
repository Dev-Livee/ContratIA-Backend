package pe.contrataia.seguimiento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.contrataia.seguimiento.entity.Documento;

import java.util.List;
import java.util.UUID;

public interface DocumentoRepository extends JpaRepository<Documento, UUID> {
    List<Documento> findByProyectoIdOrderByFechaSubidaDesc(UUID proyectoId);
    List<Documento> findByProyectoIdAndEsPublicoTrueOrderByFechaSubidaDesc(UUID proyectoId);
}
