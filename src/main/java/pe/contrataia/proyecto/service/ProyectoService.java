package pe.contrataia.proyecto.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.contrataia.auth.entity.EntidadPublica;
import pe.contrataia.auth.repository.EmpresaRepository;
import pe.contrataia.auth.repository.EntidadPublicaRepository;
import pe.contrataia.proyecto.dto.*;
import pe.contrataia.proyecto.entity.CandidatoProveedor;
import pe.contrataia.proyecto.entity.Proyecto;
import pe.contrataia.proyecto.repository.CandidatoProveedorRepository;
import pe.contrataia.proyecto.repository.ProyectoRepository;
import pe.contrataia.proveedor.service.LatInfoService;
import pe.contrataia.shared.enums.EstadoCandidato;
import pe.contrataia.shared.enums.EstadoProyecto;
import pe.contrataia.shared.exception.BusinessException;
import pe.contrataia.shared.exception.ResourceNotFoundException;
import pe.contrataia.shared.util.CodigoUnicoUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final CandidatoProveedorRepository candidatoRepository;
    private final EntidadPublicaRepository entidadRepository;
    private final EmpresaRepository empresaRepository;
    private final LatInfoService latInfoService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ProyectoResponse crear(String emailEntidad, CrearProyectoRequest req) {
        EntidadPublica entidad = entidadRepository.findByEmail(emailEntidad)
                .orElseThrow(() -> new ResourceNotFoundException("Entidad", emailEntidad));

        Proyecto proyecto = new Proyecto();
        proyecto.setEntidadPublica(entidad);
        proyecto.setTitulo(req.titulo());
        proyecto.setDescripcion(req.descripcion());
        proyecto.setPresupuesto(req.presupuesto());
        proyecto.setRubro(req.rubro());
        proyecto.setDistrito(req.distrito());
        proyecto.setProvincia(req.provincia());
        proyecto.setRegion(req.region());
        proyecto.setDireccion(req.direccion());
        proyecto.setFechaInicioPrevista(req.fechaInicioPrevista());
        proyecto.setFechaFinPrevista(req.fechaFinPrevista());
        proyecto.setPlazoMeses(req.plazoMeses());
        proyecto.setRequisitos(req.requisitos());
        proyecto.setEstado(EstadoProyecto.BORRADOR);

        return toResponse(proyectoRepository.save(proyecto));
    }

    public List<ProyectoListResponse> listarMisProyectos(String emailEntidad) {
        EntidadPublica entidad = entidadRepository.findByEmail(emailEntidad)
                .orElseThrow(() -> new ResourceNotFoundException("Entidad", emailEntidad));
        return proyectoRepository.findByEntidadPublicaIdOrderByCreatedAtDesc(entidad.getId())
                .stream().map(this::toListResponse).toList();
    }

    public ProyectoResponse obtener(String emailEntidad, UUID proyectoId) {
        Proyecto proyecto = obtenerValidandoPropietario(emailEntidad, proyectoId);
        return toResponse(proyecto);
    }

    @Transactional
    public ProyectoResponse actualizar(String emailEntidad, UUID proyectoId, ActualizarProyectoRequest req) {
        Proyecto proyecto = obtenerValidandoPropietario(emailEntidad, proyectoId);

        if (req.titulo() != null) proyecto.setTitulo(req.titulo());
        if (req.descripcion() != null) proyecto.setDescripcion(req.descripcion());
        if (req.presupuesto() != null) proyecto.setPresupuesto(req.presupuesto());
        if (req.rubro() != null) proyecto.setRubro(req.rubro());
        if (req.distrito() != null) proyecto.setDistrito(req.distrito());
        if (req.provincia() != null) proyecto.setProvincia(req.provincia());
        if (req.region() != null) proyecto.setRegion(req.region());
        if (req.direccion() != null) proyecto.setDireccion(req.direccion());
        if (req.fechaInicioPrevista() != null) proyecto.setFechaInicioPrevista(req.fechaInicioPrevista());
        if (req.fechaFinPrevista() != null) proyecto.setFechaFinPrevista(req.fechaFinPrevista());
        if (req.plazoMeses() != null) proyecto.setPlazoMeses(req.plazoMeses());
        if (req.requisitos() != null) proyecto.setRequisitos(req.requisitos());

        return toResponse(proyectoRepository.save(proyecto));
    }

    @Transactional
    public CandidatoResponse agregarCandidato(String emailEntidad, UUID proyectoId, AgregarCandidatoRequest req) {
        Proyecto proyecto = obtenerValidandoPropietario(emailEntidad, proyectoId);

        if (candidatoRepository.existsByProyectoIdAndRuc(proyectoId, req.ruc())) {
            throw new BusinessException("El proveedor ya fue agregado como candidato");
        }

        Map<String, Object> ficha = latInfoService.obtenerFichaCompleta(req.ruc());
        String snapshot = serializarJson(ficha);

        CandidatoProveedor candidato = new CandidatoProveedor();
        candidato.setProyecto(proyecto);
        candidato.setRuc(req.ruc());
        candidato.setRazonSocial((String) ficha.getOrDefault("razon_social", ""));
        candidato.setSnapshotLatinfo(snapshot);
        candidato.setEstado(EstadoCandidato.CANDIDATO);

        // Vincular si está registrado en el sistema
        empresaRepository.findByRuc(req.ruc()).ifPresent(candidato::setEmpresa);

        return toCandidatoResponse(candidatoRepository.save(candidato));
    }

    public List<CandidatoResponse> listarCandidatos(String emailEntidad, UUID proyectoId) {
        obtenerValidandoPropietario(emailEntidad, proyectoId);
        return candidatoRepository.findByProyectoIdOrderByFechaAgregadoAsc(proyectoId)
                .stream().map(this::toCandidatoResponse).toList();
    }

    @Transactional
    public void eliminarCandidato(String emailEntidad, UUID proyectoId, UUID candidatoId) {
        obtenerValidandoPropietario(emailEntidad, proyectoId);
        CandidatoProveedor candidato = candidatoRepository.findById(candidatoId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidato", candidatoId));
        if (candidato.getEstado() == EstadoCandidato.SELECCIONADO) {
            throw new BusinessException("No se puede eliminar el candidato adjudicado");
        }
        candidatoRepository.delete(candidato);
    }

    public List<CandidatoResponse> obtenerComparador(String emailEntidad, UUID proyectoId) {
        return listarCandidatos(emailEntidad, proyectoId);
    }

    @Transactional
    public ProyectoResponse adjudicar(String emailEntidad, UUID proyectoId, AdjudicarRequest req) {
        Proyecto proyecto = obtenerValidandoPropietario(emailEntidad, proyectoId);

        CandidatoProveedor ganador = candidatoRepository
                .findByProyectoIdAndRuc(proyectoId, req.rucEmpresaGanadora())
                .orElseThrow(() -> new BusinessException("El RUC no está en la lista de candidatos"));

        // Marcar todos como rechazados, luego seleccionar al ganador
        candidatoRepository.findByProyectoIdOrderByFechaAgregadoAsc(proyectoId).forEach(c -> {
            c.setEstado(c.getId().equals(ganador.getId())
                    ? EstadoCandidato.SELECCIONADO
                    : EstadoCandidato.RECHAZADO);
            candidatoRepository.save(c);
        });

        // Generar código único de obra si no existe
        if (proyecto.getCodigoUnico() == null) {
            String codigo;
            do { codigo = CodigoUnicoUtil.generar(); }
            while (proyectoRepository.existsByCodigoUnico(codigo));
            proyecto.setCodigoUnico(codigo);
        }

        proyecto.setEstado(EstadoProyecto.ADJUDICADO);
        proyecto.setFechaAdjudicacion(LocalDateTime.now());

        // Vincular empresa si está registrada
        empresaRepository.findByRuc(req.rucEmpresaGanadora())
                .ifPresent(proyecto::setEmpresaAdjudicada);

        return toResponse(proyectoRepository.save(proyecto));
    }

    @Transactional
    public ProyectoResponse actualizarEstado(String emailEntidad, UUID proyectoId, ActualizarEstadoRequest req) {
        Proyecto proyecto = obtenerValidandoPropietario(emailEntidad, proyectoId);

        if (req.estado() == EstadoProyecto.ADJUDICADO) {
            throw new BusinessException("Para adjudicar usa el endpoint /adjudicar");
        }

        if (req.estado() != EstadoProyecto.BORRADOR && proyecto.getCodigoUnico() == null) {
            String codigo;
            do { codigo = CodigoUnicoUtil.generar(); }
            while (proyectoRepository.existsByCodigoUnico(codigo));
            proyecto.setCodigoUnico(codigo);
        }

        proyecto.setEstado(req.estado());
        return toResponse(proyectoRepository.save(proyecto));
    }

    private Proyecto obtenerValidandoPropietario(String emailEntidad, UUID proyectoId) {
        EntidadPublica entidad = entidadRepository.findByEmail(emailEntidad)
                .orElseThrow(() -> new ResourceNotFoundException("Entidad", emailEntidad));
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto", proyectoId));
        if (!proyecto.getEntidadPublica().getId().equals(entidad.getId())) {
            throw new BusinessException("No tienes acceso a este proyecto", HttpStatus.FORBIDDEN);
        }
        return proyecto;
    }

    private String serializarJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private ProyectoResponse toResponse(Proyecto p) {
        var entidadResumen = new ProyectoResponse.EntidadResumen(
                p.getEntidadPublica().getId(),
                p.getEntidadPublica().getRazonSocial(),
                p.getEntidadPublica().getRuc(),
                p.getEntidadPublica().getDistrito()
        );
        ProyectoResponse.EmpresaResumen empresaResumen = null;
        if (p.getEmpresaAdjudicada() != null) {
            empresaResumen = new ProyectoResponse.EmpresaResumen(
                    p.getEmpresaAdjudicada().getId(),
                    p.getEmpresaAdjudicada().getRazonSocial(),
                    p.getEmpresaAdjudicada().getRuc()
            );
        }
        return new ProyectoResponse(p.getId(), p.getTitulo(), p.getDescripcion(), p.getPresupuesto(),
                p.getRubro(), p.getDistrito(), p.getProvincia(), p.getRegion(), p.getDireccion(),
                p.getFechaInicioPrevista(), p.getFechaFinPrevista(), p.getPlazoMeses(), p.getRequisitos(),
                p.getEstado(), p.getCodigoUnico(), p.getAvanceFisico(), p.getAvanceFinanciero(),
                p.getFechaAdjudicacion(), entidadResumen, empresaResumen, p.getCreatedAt());
    }

    private ProyectoListResponse toListResponse(Proyecto p) {
        return new ProyectoListResponse(p.getId(), p.getTitulo(), p.getRubro(), p.getDistrito(),
                p.getRegion(), p.getPresupuesto(), p.getEstado(), p.getCodigoUnico(),
                p.getAvanceFisico(), p.getCreatedAt());
    }

    private CandidatoResponse toCandidatoResponse(CandidatoProveedor c) {
        return new CandidatoResponse(c.getId(), c.getRuc(), c.getRazonSocial(),
                c.getEstado(), c.getSnapshotLatinfo(), c.getFechaAgregado());
    }
}
