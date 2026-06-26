package pe.contrataia.seguimiento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.contrataia.seguimiento.entity.Avance;

import java.util.List;
import java.util.UUID;

public interface AvanceRepository extends JpaRepository<Avance, UUID> {
    List<Avance> findByProyectoIdOrderByFechaRegistroDesc(UUID proyectoId);
}
