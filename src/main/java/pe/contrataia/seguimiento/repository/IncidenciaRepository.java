package pe.contrataia.seguimiento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.contrataia.seguimiento.entity.Incidencia;
import pe.contrataia.shared.enums.EstadoIncidencia;

import java.util.List;
import java.util.UUID;

public interface IncidenciaRepository extends JpaRepository<Incidencia, UUID> {
    List<Incidencia> findByProyectoIdOrderByFechaReporteDesc(UUID proyectoId);
    List<Incidencia> findByProyectoIdAndEstadoOrderByFechaReporteDesc(UUID proyectoId, EstadoIncidencia estado);
}
