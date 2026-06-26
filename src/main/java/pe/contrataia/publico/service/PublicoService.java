package pe.contrataia.publico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.contrataia.proyecto.entity.Proyecto;
import pe.contrataia.proyecto.repository.ProyectoRepository;
import pe.contrataia.publico.dto.ObraDetalleResponse;
import pe.contrataia.publico.dto.ObraPublicaResponse;
import pe.contrataia.publico.dto.TimelineEventoResponse;
import pe.contrataia.seguimiento.dto.AvanceResponse;
import pe.contrataia.seguimiento.dto.DocumentoResponse;
import pe.contrataia.seguimiento.dto.EvidenciaResponse;
import pe.contrataia.seguimiento.dto.HitoResponse;
import pe.contrataia.seguimiento.entity.Avance;
import pe.contrataia.seguimiento.entity.Documento;
import pe.contrataia.seguimiento.entity.Evidencia;
import pe.contrataia.seguimiento.entity.Hito;
import pe.contrataia.seguimiento.repository.AvanceRepository;
import pe.contrataia.seguimiento.repository.DocumentoRepository;
import pe.contrataia.seguimiento.repository.EvidenciaRepository;
import pe.contrataia.seguimiento.repository.HitoRepository;
import pe.contrataia.shared.enums.EstadoProyecto;
import pe.contrataia.shared.exception.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicoService {

    private final ProyectoRepository proyectoRepository;
    private final HitoRepository hitoRepository;
    private final AvanceRepository avanceRepository;
    private final EvidenciaRepository evidenciaRepository;
    private final DocumentoRepository documentoRepository;

    public Page<ObraPublicaResponse> buscarObras(String distrito, EstadoProyecto estado, String rubro,
                                                   java.math.BigDecimal presupuestoMin, java.math.BigDecimal presupuestoMax,
                                                   int page, int size) {
        return proyectoRepository.buscarPublico(distrito, estado, rubro, presupuestoMin, presupuestoMax, PageRequest.of(page, size))
                .map(this::toObraPublicaResponse);
    }

    public ObraDetalleResponse obtenerDetalle(String codigoUnico) {
        Proyecto proyecto = proyectoRepository.findByCodigoUnico(codigoUnico)
                .orElseThrow(() -> new ResourceNotFoundException("Obra", codigoUnico));

        List<HitoResponse> hitos = hitoRepository.findByProyectoIdOrderByOrdenAsc(proyecto.getId())
                .stream().map(this::toHitoResponse).toList();

        List<AvanceResponse> avances = avanceRepository.findByProyectoIdOrderByFechaRegistroDesc(proyecto.getId())
                .stream().limit(5).map(this::toAvanceResponse).toList();

        List<EvidenciaResponse> evidencias = evidenciaRepository
                .findByProyectoIdAndEsPublicoTrueOrderByFechaSubidaDesc(proyecto.getId())
                .stream().map(this::toEvidenciaResponse).toList();

        List<DocumentoResponse> documentos = documentoRepository
                .findByProyectoIdAndEsPublicoTrueOrderByFechaSubidaDesc(proyecto.getId())
                .stream().map(this::toDocumentoResponse).toList();

        String entidadNombre = proyecto.getEntidadPublica().getRazonSocial();
        String entidadTipo = proyecto.getEntidadPublica().getTipo();
        String empresaNombre = proyecto.getEmpresaAdjudicada() != null ? proyecto.getEmpresaAdjudicada().getRazonSocial() : null;
        String empresaRuc = proyecto.getEmpresaAdjudicada() != null ? proyecto.getEmpresaAdjudicada().getRuc() : null;

        return new ObraDetalleResponse(
                proyecto.getId(), proyecto.getCodigoUnico(), proyecto.getTitulo(), proyecto.getDescripcion(),
                proyecto.getRequisitos(), proyecto.getPresupuesto(), proyecto.getRubro(),
                proyecto.getDistrito(), proyecto.getProvincia(), proyecto.getRegion(), proyecto.getDireccion(),
                proyecto.getFechaInicioPrevista(), proyecto.getFechaFinPrevista(), proyecto.getPlazoMeses(),
                proyecto.getEstado(), proyecto.getAvanceFisico(), proyecto.getAvanceFinanciero(),
                proyecto.getFechaAdjudicacion(),
                entidadNombre, entidadTipo, empresaNombre, empresaRuc,
                hitos, avances, evidencias, documentos
        );
    }

    public List<TimelineEventoResponse> obtenerTimeline(String codigoUnico) {
        Proyecto proyecto = proyectoRepository.findByCodigoUnico(codigoUnico)
                .orElseThrow(() -> new ResourceNotFoundException("Obra", codigoUnico));

        List<TimelineEventoResponse> eventos = new ArrayList<>();

        eventos.add(new TimelineEventoResponse(
                "CREACION", "Proyecto registrado: " + proyecto.getTitulo(),
                proyecto.getCreatedAt(), "Estado inicial: " + EstadoProyecto.BORRADOR.name()
        ));

        if (proyecto.getFechaAdjudicacion() != null) {
            String empresa = proyecto.getEmpresaAdjudicada() != null ? proyecto.getEmpresaAdjudicada().getRazonSocial() : "Empresa";
            eventos.add(new TimelineEventoResponse(
                    "ADJUDICACION", "Obra adjudicada a " + empresa,
                    proyecto.getFechaAdjudicacion(), "RUC: " + (proyecto.getEmpresaAdjudicada() != null ? proyecto.getEmpresaAdjudicada().getRuc() : "")
            ));
        }

        hitoRepository.findByProyectoIdOrderByOrdenAsc(proyecto.getId()).forEach(h -> {
            java.time.LocalDateTime fecha = h.getFechaReal() != null
                    ? h.getFechaReal().atStartOfDay()
                    : (h.getFechaPrevista() != null ? h.getFechaPrevista().atStartOfDay() : h.getCreatedAt());
            eventos.add(new TimelineEventoResponse(
                    "HITO", h.getNombre(), fecha, "Estado: " + h.getEstado().name()
            ));
        });

        avanceRepository.findByProyectoIdOrderByFechaRegistroDesc(proyecto.getId()).forEach(a ->
                eventos.add(new TimelineEventoResponse(
                        "AVANCE",
                        String.format("Avance físico: %.1f%% / financiero: %.1f%%",
                                a.getAvanceFisico(), a.getAvanceFinanciero() != null ? a.getAvanceFinanciero() : a.getAvanceFisico()),
                        a.getFechaRegistro(), a.getDescripcion()
                ))
        );

        evidenciaRepository.findByProyectoIdAndEsPublicoTrueOrderByFechaSubidaDesc(proyecto.getId()).forEach(e ->
                eventos.add(new TimelineEventoResponse(
                        "EVIDENCIA", e.getNombre() != null ? e.getNombre() : "Evidencia de obra",
                        e.getFechaSubida(), e.getTipo().name()
                ))
        );

        eventos.sort(Comparator.comparing(TimelineEventoResponse::fecha));
        return eventos;
    }

    public List<String> listarDistritos() {
        return proyectoRepository.findDistritosConObras();
    }

    private ObraPublicaResponse toObraPublicaResponse(Proyecto p) {
        String empresaNombre = p.getEmpresaAdjudicada() != null ? p.getEmpresaAdjudicada().getRazonSocial() : null;
        return new ObraPublicaResponse(
                p.getId(), p.getCodigoUnico(), p.getTitulo(), p.getDescripcion(),
                p.getPresupuesto(), p.getRubro(), p.getDistrito(), p.getProvincia(), p.getRegion(),
                p.getDireccion(), p.getFechaInicioPrevista(), p.getFechaFinPrevista(), p.getPlazoMeses(),
                p.getEstado(), p.getAvanceFisico(), p.getAvanceFinanciero(),
                p.getEntidadPublica().getRazonSocial(), empresaNombre
        );
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
        return new EvidenciaResponse(e.getId(), e.getTipo(), e.getNombre(), e.getUrl(),
                e.getDescripcion(), e.isEsPublico(), e.getHito() != null ? e.getHito().getId() : null, e.getFechaSubida());
    }

    private DocumentoResponse toDocumentoResponse(Documento d) {
        return new DocumentoResponse(d.getId(), d.getTipo(), d.getNombre(), d.getUrl(),
                d.getVersion(), d.isEsPublico(), d.getFechaSubida());
    }
}
