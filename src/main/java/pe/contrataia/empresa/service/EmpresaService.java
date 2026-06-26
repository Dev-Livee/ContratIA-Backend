package pe.contrataia.empresa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.contrataia.auth.entity.Empresa;
import pe.contrataia.auth.repository.EmpresaRepository;
import pe.contrataia.empresa.dto.*;
import pe.contrataia.empresa.entity.Certificacion;
import pe.contrataia.empresa.entity.DocumentoEmpresa;
import pe.contrataia.empresa.entity.ExperienciaEmpresa;
import pe.contrataia.empresa.repository.CertificacionRepository;
import pe.contrataia.empresa.repository.DocumentoEmpresaRepository;
import pe.contrataia.empresa.repository.ExperienciaEmpresaRepository;
import pe.contrataia.proyecto.repository.CandidatoProveedorRepository;
import pe.contrataia.shared.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final ExperienciaEmpresaRepository experienciaRepository;
    private final CertificacionRepository certificacionRepository;
    private final DocumentoEmpresaRepository documentoRepository;
    private final CandidatoProveedorRepository candidatoRepository;

    public EmpresaPerfilResponse obtenerPerfil(String email) {
        Empresa empresa = empresaRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", email));
        return toPerfilResponse(empresa);
    }

    @Transactional
    public EmpresaPerfilResponse actualizarPerfil(String email, UpdatePerfilRequest req) {
        Empresa empresa = empresaRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", email));

        if (req.sector() != null) empresa.setSector(req.sector());
        if (req.direccion() != null) empresa.setDireccion(req.direccion());
        if (req.telefono() != null) empresa.setTelefono(req.telefono());
        if (req.sitioWeb() != null) empresa.setSitioWeb(req.sitioWeb());
        if (req.descripcion() != null) empresa.setDescripcion(req.descripcion());
        if (req.representanteLegal() != null) empresa.setRepresentanteLegal(req.representanteLegal());

        return toPerfilResponse(empresaRepository.save(empresa));
    }

    @Transactional
    public ExperienciaResponse agregarExperiencia(String email, ExperienciaRequest req) {
        Empresa empresa = empresaRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", email));

        ExperienciaEmpresa exp = new ExperienciaEmpresa();
        exp.setEmpresa(empresa);
        exp.setDescripcion(req.descripcion());
        exp.setEntidadContratante(req.entidadContratante());
        exp.setMonto(req.monto());
        exp.setRubro(req.rubro());
        exp.setRegion(req.region());
        exp.setFechaInicio(req.fechaInicio());
        exp.setFechaFin(req.fechaFin());

        return toExperienciaResponse(experienciaRepository.save(exp));
    }

    public List<ExperienciaResponse> listarExperiencias(String email) {
        Empresa empresa = empresaRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", email));
        return experienciaRepository.findByEmpresaIdOrderByFechaInicioDesc(empresa.getId())
                .stream().map(this::toExperienciaResponse).toList();
    }

    @Transactional
    public void eliminarExperiencia(String email, UUID experienciaId) {
        ExperienciaEmpresa exp = experienciaRepository.findById(experienciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Experiencia", experienciaId));
        validarPropiedadEmpresa(exp.getEmpresa().getEmail(), email);
        experienciaRepository.delete(exp);
    }

    @Transactional
    public CertificacionResponse agregarCertificacion(String email, CertificacionRequest req) {
        Empresa empresa = empresaRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", email));

        Certificacion cert = new Certificacion();
        cert.setEmpresa(empresa);
        cert.setNombre(req.nombre());
        cert.setEntidadEmisora(req.entidadEmisora());
        cert.setFechaEmision(req.fechaEmision());
        cert.setFechaVencimiento(req.fechaVencimiento());
        cert.setDocumentoUrl(req.documentoUrl());

        return toCertificacionResponse(certificacionRepository.save(cert));
    }

    public List<CertificacionResponse> listarCertificaciones(String email) {
        Empresa empresa = empresaRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", email));
        return certificacionRepository.findByEmpresaIdOrderByFechaEmisionDesc(empresa.getId())
                .stream().map(this::toCertificacionResponse).toList();
    }

    @Transactional
    public void eliminarCertificacion(String email, UUID certId) {
        Certificacion cert = certificacionRepository.findById(certId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificacion", certId));
        validarPropiedadEmpresa(cert.getEmpresa().getEmail(), email);
        certificacionRepository.delete(cert);
    }

    @Transactional
    public void agregarDocumento(String email, String tipo, String nombre, String url) {
        Empresa empresa = empresaRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", email));

        DocumentoEmpresa doc = new DocumentoEmpresa();
        doc.setEmpresa(empresa);
        doc.setTipo(tipo);
        doc.setNombre(nombre);
        doc.setUrl(url);
        documentoRepository.save(doc);
    }

    public List<EvaluacionHistoricaResponse> consultarEvaluacionesHistoricas(String email) {
        Empresa empresa = empresaRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", email));
        return candidatoRepository.findByEmpresaIdOrderByFechaAgregadoDesc(empresa.getId())
                .stream()
                .map(c -> new EvaluacionHistoricaResponse(
                        c.getProyecto().getId(),
                        c.getProyecto().getCodigoUnico(),
                        c.getProyecto().getTitulo(),
                        c.getProyecto().getRubro(),
                        c.getProyecto().getDistrito(),
                        c.getProyecto().getPresupuesto(),
                        c.getProyecto().getEstado(),
                        c.getEstado(),
                        c.getFechaAgregado(),
                        c.getProyecto().getEntidadPublica().getRazonSocial()
                ))
                .toList();
    }

    private void validarPropiedadEmpresa(String ownerEmail, String requestEmail) {
        if (!ownerEmail.equals(requestEmail)) {
            throw new ResourceNotFoundException("Recurso no encontrado");
        }
    }

    private EmpresaPerfilResponse toPerfilResponse(Empresa e) {
        return new EmpresaPerfilResponse(e.getId(), e.getRuc(), e.getRazonSocial(), e.getEstadoSunat(),
                e.getCondicion(), e.getSector(), e.getDireccion(), e.getTelefono(), e.getSitioWeb(),
                e.getDescripcion(), e.getRepresentanteLegal(), e.getDniRepresentante(), e.getFechaInscripcion(),
                e.getEmail());
    }

    private ExperienciaResponse toExperienciaResponse(ExperienciaEmpresa e) {
        return new ExperienciaResponse(e.getId(), e.getDescripcion(), e.getEntidadContratante(),
                e.getMonto(), e.getRubro(), e.getRegion(), e.getFechaInicio(), e.getFechaFin());
    }

    private CertificacionResponse toCertificacionResponse(Certificacion c) {
        return new CertificacionResponse(c.getId(), c.getNombre(), c.getEntidadEmisora(),
                c.getFechaEmision(), c.getFechaVencimiento(), c.getDocumentoUrl());
    }
}
