package pe.contrataia.seguimiento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.contrataia.seguimiento.entity.Hito;

import java.util.List;
import java.util.UUID;

public interface HitoRepository extends JpaRepository<Hito, UUID> {
    List<Hito> findByProyectoIdOrderByOrdenAsc(UUID proyectoId);
}
