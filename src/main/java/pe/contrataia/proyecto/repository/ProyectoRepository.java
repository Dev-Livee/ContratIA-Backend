package pe.contrataia.proyecto.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.contrataia.proyecto.entity.Proyecto;
import pe.contrataia.shared.enums.EstadoProyecto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProyectoRepository extends JpaRepository<Proyecto, UUID> {

    List<Proyecto> findByEntidadPublicaIdOrderByCreatedAtDesc(UUID entidadId);

    Optional<Proyecto> findByCodigoUnico(String codigoUnico);

    boolean existsByCodigoUnico(String codigoUnico);

    @Query(
        value = "SELECT p FROM Proyecto p"
              + " WHERE (:distrito IS NULL OR LOWER(p.distrito) = LOWER(:distrito))"
              + " AND (:rubro IS NULL OR LOWER(p.rubro) LIKE LOWER(CONCAT('%', :rubro, '%')))"
              + " AND (:presupuestoMin IS NULL OR p.presupuesto >= :presupuestoMin)"
              + " AND (:presupuestoMax IS NULL OR p.presupuesto <= :presupuestoMax)"
              + " AND p.estado <> pe.contrataia.shared.enums.EstadoProyecto.BORRADOR"
              + " AND (:#{#estado} IS NULL OR p.estado = :estado)"
              + " ORDER BY p.createdAt DESC",
        countQuery = "SELECT COUNT(p) FROM Proyecto p"
              + " WHERE (:distrito IS NULL OR LOWER(p.distrito) = LOWER(:distrito))"
              + " AND (:rubro IS NULL OR LOWER(p.rubro) LIKE LOWER(CONCAT('%', :rubro, '%')))"
              + " AND (:presupuestoMin IS NULL OR p.presupuesto >= :presupuestoMin)"
              + " AND (:presupuestoMax IS NULL OR p.presupuesto <= :presupuestoMax)"
              + " AND p.estado <> pe.contrataia.shared.enums.EstadoProyecto.BORRADOR"
              + " AND (:#{#estado} IS NULL OR p.estado = :estado)"
    )
    Page<Proyecto> buscarPublico(
            @Param("distrito") String distrito,
            @Param("estado") EstadoProyecto estado,
            @Param("rubro") String rubro,
            @Param("presupuestoMin") java.math.BigDecimal presupuestoMin,
            @Param("presupuestoMax") java.math.BigDecimal presupuestoMax,
            Pageable pageable
    );

    @Query("SELECT DISTINCT p.distrito FROM Proyecto p WHERE p.distrito IS NOT NULL AND p.estado <> pe.contrataia.shared.enums.EstadoProyecto.BORRADOR ORDER BY p.distrito")
    List<String> findDistritosConObras();
}
