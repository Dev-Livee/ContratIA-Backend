package pe.contrataia.seguimiento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.contrataia.seguimiento.entity.Evidencia;

import java.util.List;
import java.util.UUID;

public interface EvidenciaRepository extends JpaRepository<Evidencia, UUID> {
    List<Evidencia> findByProyectoIdOrderByFechaSubidaDesc(UUID proyectoId);
    List<Evidencia> findByProyectoIdAndEsPublicoTrueOrderByFechaSubidaDesc(UUID proyectoId);
}
