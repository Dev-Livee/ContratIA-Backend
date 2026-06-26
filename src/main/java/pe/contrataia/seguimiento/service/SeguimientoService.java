package pe.contrataia.seguimiento.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.contrataia.auth.entity.EntidadPublica;
import pe.contrataia.auth.repository.EntidadPublicaRepository;
import pe.contrataia.proyecto.entity.Proyecto;
import pe.contrataia.proyecto.repository.ProyectoRepository;
import pe.contrataia.seguimiento.dto.*;
import pe.contrataia.seguimiento.entity.Avance;
import pe.contrataia.seguimiento.entity.Documento;
import pe.contrataia.seguimiento.entity.Evidencia;
import pe.contrataia.seguimiento.entity.Hito;
import pe.contrataia.seguimiento.entity.Incidencia;
import pe.contrataia.seguimiento.repository.*;
import pe.contrataia.shared.enums.EstadoHito;
import pe.contrataia.shared.enums.EstadoIncidencia;
import pe.contrataia.shared.exception.BusinessException;
import pe.contrataia.shared.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeguimientoService {

    private final ProyectoRepository proyectoRepository;
    private final EntidadPublicaRepository entidadRepository;
    private final HitoRepository hitoRepository;
    private final AvanceRepository avanceRepository;
    private final EvidenciaRepository evidenciaRepository;
    private final DocumentoRepository documentoRepository;
    private final IncidenciaRepository incidenciaRepository;

    @Transactional
    public HitoResponse crearHito(String emailEntidad, UUID proyectoId, HitoRequest req) {
        Proyecto proyecto = obtenerValidandoPropietario(emailEntidad, proyectoId);

        Hito hito = new Hito();
        hito.setProyecto(proyecto);
        hito.setNombre(req.nombre());
        hito.setDescripcion(req.descripcion());
        hito.setFechaPrevista(req.fechaPrevista());
        hito.setFechaReal(req.fechaReal());
        hito.setEstado(req.estado() != null ? req.estado() : EstadoHito.PENDIENTE);
        hito.setOrden(req.orden() != null ? req.orden() : 0);

        return toHitoResponse(hitoRepository.save(hito));
    }

    public List<HitoResponse> listarHitos(String emailEntidad, UUID proyectoId) {
        obtenerValidandoPropietario(emailEntidad, proyectoId);
        return hitoRepository.findByProyectoIdOrderByOrdenAsc(proyectoId)
                .stream().map(this::toHitoResponse).toList();
    }

    @Transactional
    public HitoResponse actualizarHito(String emailEntidad, UUID proyectoId, UUID hitoId, HitoRequest req) {
        obtenerValidandoPropietario(emailEntidad, proyectoId);
        Hito hito = hitoRepository.findById(hitoId)
                .orElseThrow(() -> new ResourceNotFoundException("Hito", hitoId));

        if (req.nombre() != null) hito.setNombre(req.nombre());
        if (req.descripcion() != null) hito.setDescripcion(req.descripcion());
        if (req.fechaPrevista() != null) hito.setFechaPrevista(req.fechaPrevista());
        if (req.fechaReal() != null) hito.setFechaReal(req.fechaReal());
        if (req.estado() != null) hito.setEstado(req.estado());
        if (req.orden() != null) hito.setOrden(req.orden());

        return toHitoResponse(hitoRepository.save(hito));
    }

    @Transactional
    public AvanceResponse registrarAvance(String emailEntidad, UUID proyectoId, AvanceRequest req) {
        Proyecto proyecto = obtenerValidandoPropietario(emailEntidad, proyectoId);

        Avance avance = new Avance();
        avance.setProyecto(proyecto);
        avance.setAvanceFisico(req.avanceFisico());
        avance.setAvanceFinanciero(req.avanceFinanciero());
        avance.setDescripcion(req.descripcion());

        // Actualizar avance consolidado en el proyecto
        proyecto.setAvanceFisico(req.avanceFisico());
        if (req.avanceFinanciero() != null) proyecto.setAvanceFinanciero(req.avanceFinanciero());
        proyectoRepository.save(proyecto);

        return toAvanceResponse(avanceRepository.save(avance));
    }

    public List<AvanceResponse> listarAvances(String emailEntidad, UUID proyectoId) {
        obtenerValidandoPropietario(emailEntidad, proyectoId);
        return avanceRepository.findByProyectoIdOrderByFechaRegistroDesc(proyectoId)
                .stream().map(this::toAvanceResponse).toList();
    }

    @Transactional
    public EvidenciaResponse agregarEvidencia(String emailEntidad, UUID proyectoId, EvidenciaRequest req) {
        Proyecto proyecto = obtenerValidandoPropietario(emailEntidad, proyectoId);

        Evidencia evidencia = new Evidencia();
        evidencia.setProyecto(proyecto);
        evidencia.setTipo(req.tipo());
        evidencia.setNombre(req.nombre());
        evidencia.setUrl(req.url());
        evidencia.setDescripcion(req.descripcion());
        evidencia.setEsPublico(req.esPublico());

        if (req.hitoId() != null) {
            hitoRepository.findById(req.hitoId()).ifPresent(evidencia::setHito);
        }

        return toEvidenciaResponse(evidenciaRepository.save(evidencia));
    }

    public List<EvidenciaResponse> listarEvidencias(String emailEntidad, UUID proyectoId) {
        obtenerValidandoPropietario(emailEntidad, proyectoId);
        return evidenciaRepository.findByProyectoIdOrderByFechaSubidaDesc(proyectoId)
                .stream().map(this::toEvidenciaResponse).toList();
    }

    @Transactional
    public DocumentoResponse agregarDocumento(String emailEntidad, UUID proyectoId, DocumentoRequest req) {
        Proyecto proyecto = obtenerValidandoPropietario(emailEntidad, proyectoId);

        Documento doc = new Documento();
        doc.setProyecto(proyecto);
        doc.setTipo(req.tipo());
        doc.setNombre(req.nombre());
        doc.setUrl(req.url());
        doc.setEsPublico(req.esPublico());

        return toDocumentoResponse(documentoRepository.save(doc));
    }

    public List<DocumentoResponse> listarDocumentos(String emailEntidad, UUID proyectoId) {
        obtenerValidandoPropietario(emailEntidad, proyectoId);
        return documentoRepository.findByProyectoIdOrderByFechaSubidaDesc(proyectoId)
                .stream().map(this::toDocumentoResponse).toList();
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

    private HitoResponse toHitoResponse(Hito h) {
        return new HitoResponse(h.getId(), h.getNombre(), h.getDescripcion(),
                h.getFechaPrevista(), h.getFechaReal(), h.getEstado(), h.getOrden());
    }

    private AvanceResponse toAvanceResponse(Avance a) {
        return new AvanceResponse(a.getId(), a.getAvanceFisico(), a.getAvanceFinanciero(),
                a.getDescripcion(), a.getFechaRegistro());
    }

    private EvidenciaResponse toEvidenciaResponse(Evidencia e) {
        UUID hitoId = e.getHito() != null ? e.getHito().getId() : null;
        return new EvidenciaResponse(e.getId(), e.getTipo(), e.getNombre(), e.getUrl(),
                e.getDescripcion(), e.isEsPublico(), hitoId, e.getFechaSubida());
    }

    private DocumentoResponse toDocumentoResponse(Documento d) {
        return new DocumentoResponse(d.getId(), d.getTipo(), d.getNombre(), d.getUrl(),
                d.getVersion(), d.isEsPublico(), d.getFechaSubida());
    }

    // ─── Incidencias ─────────────────────────────────────────────────────────

    @Transactional
    public IncidenciaResponse reportarIncidencia(String emailEntidad, UUID proyectoId, IncidenciaRequest req) {
        Proyecto proyecto = obtenerValidandoPropietario(emailEntidad, proyectoId);

        Incidencia inc = new Incidencia();
        inc.setProyecto(proyecto);
        inc.setTitulo(req.titulo());
        inc.setDescripcion(req.descripcion());
        inc.setTipo(req.tipo());
        inc.setSeveridad(req.severidad());
        if (req.hitoId() != null) {
            hitoRepository.findById(req.hitoId()).ifPresent(inc::setHito);
        }

        return toIncidenciaResponse(incidenciaRepository.save(inc));
    }

    public List<IncidenciaResponse> listarIncidencias(String emailEntidad, UUID proyectoId) {
        obtenerValidandoPropietario(emailEntidad, proyectoId);
        return incidenciaRepository.findByProyectoIdOrderByFechaReporteDesc(proyectoId)
                .stream().map(this::toIncidenciaResponse).toList();
    }

    @Transactional
    public IncidenciaResponse resolverIncidencia(String emailEntidad, UUID proyectoId, UUID incidenciaId,
                                                  ResolverIncidenciaRequest req) {
        obtenerValidandoPropietario(emailEntidad, proyectoId);
        Incidencia inc = incidenciaRepository.findById(incidenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Incidencia", incidenciaId));
        inc.setEstado(EstadoIncidencia.RESUELTA);
        inc.setResolucion(req.resolucion());
        inc.setFechaResolucion(java.time.LocalDateTime.now());
        return toIncidenciaResponse(incidenciaRepository.save(inc));
    }

    private IncidenciaResponse toIncidenciaResponse(Incidencia i) {
        UUID hitoId = i.getHito() != null ? i.getHito().getId() : null;
        return new IncidenciaResponse(i.getId(), i.getTitulo(), i.getDescripcion(),
                i.getTipo(), i.getSeveridad(), i.getEstado(), hitoId,
                i.getFechaReporte(), i.getFechaResolucion(), i.getResolucion());
    }
}
