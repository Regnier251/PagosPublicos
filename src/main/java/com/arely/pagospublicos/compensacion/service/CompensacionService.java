package com.arely.pagospublicos.compensacion.service;

import com.arely.pagospublicos.compensacion.model.*;
import com.arely.pagospublicos.compensacion.model.dto.CompensacionForm;
import com.arely.pagospublicos.compensacion.model.dto.CompensacionForm.Linea;
import com.arely.pagospublicos.compensacion.repository.CompensacionRepository;
import com.arely.pagospublicos.compensacion.repository.DetalleCompensacionRepository;
import com.arely.pagospublicos.tercero.model.*;
import com.arely.pagospublicos.tercero.repository.CuentaBancariaRepository;
import com.arely.pagospublicos.tercero.repository.DocumentoRepository;
import com.arely.pagospublicos.tercero.service.TerceroService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CompensacionService {

    private static final BigDecimal CERO = BigDecimal.ZERO;

    private final CompensacionRepository compensacionRepository;
    private final DetalleCompensacionRepository detalleRepository;
    private final DocumentoRepository documentoRepository;
    private final CuentaBancariaRepository cuentaRepository;
    private final TerceroService terceroService;

    public CompensacionService(CompensacionRepository compensacionRepository,
                               DetalleCompensacionRepository detalleRepository,
                               DocumentoRepository documentoRepository,
                               CuentaBancariaRepository cuentaRepository,
                               TerceroService terceroService) {
        this.compensacionRepository = compensacionRepository;
        this.detalleRepository = detalleRepository;
        this.documentoRepository = documentoRepository;
        this.cuentaRepository = cuentaRepository;
        this.terceroService = terceroService;
    }

    public List<Compensacion> listarPorTercero(Long terceroId) {
        return compensacionRepository.findByTerceroId(terceroId,
                Sort.by("id").descending());
    }

    public Compensacion buscarPorId(Long id) {
        return compensacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Compensación no encontrada: " + id));
    }

    public List<DetalleCompensacion> detalleDe(Long compensacionId) {
        return detalleRepository.findByCompensacionId(compensacionId);
    }

    /**
     * Crea y aplica la compensación en UNA sola transacción:
     * encabezado + detalle + decremento de saldos. O todo, o nada.
     */
    @Transactional
    public Compensacion aplicar(Long terceroId, CompensacionForm form) {
        Tercero tercero = terceroService.buscarPorId(terceroId);

        // ── 1. Líneas capturadas ──
        List<Linea> lineas = form.lineasConMonto();
        if (lineas.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debes capturar al menos un monto a aplicar");
        }

        // ── 2. Validar cada documento y acumular por tipo ──
        BigDecimal totalCxc = CERO;
        BigDecimal totalCxp = CERO;
        List<DetalleCompensacion> detalles = new ArrayList<>();

        for (Linea linea : lineas) {
            Documento doc = documentoRepository.findById(linea.getDocumentoId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Documento no encontrado: " + linea.getDocumentoId()));

            if (!doc.getTercero().getId().equals(terceroId)) {
                throw new IllegalArgumentException(
                        "El documento " + doc.getFolio() + " no pertenece a este tercero");
            }
            if (doc.getEstatus() != EstatusDocumento.VIGENTE) {
                throw new IllegalArgumentException(
                        "El documento " + doc.getFolio() + " no está vigente");
            }
            if (linea.getMonto().compareTo(doc.getSaldoPendiente()) > 0) {
                throw new IllegalArgumentException(String.format(
                        "El monto para %s (%s) excede su saldo disponible (%s)",
                        doc.getFolio(), linea.getMonto().toPlainString(),
                        doc.getSaldoPendiente().toPlainString()));
            }

            DetalleCompensacion detalle = new DetalleCompensacion();
            detalle.setDocumento(doc);
            detalle.setMontoAplicado(linea.getMonto());
            detalles.add(detalle);

            if (doc.getTipo() == TipoDocumento.CXC) {
                totalCxc = totalCxc.add(linea.getMonto());
            } else {
                totalCxp = totalCxp.add(linea.getMonto());
            }
        }

        // ── 3. Regla del mínimo común: Σ CxC = Σ CxP ──
        if (totalCxc.compareTo(totalCxp) != 0) {
            throw new IllegalArgumentException(String.format(
                    "Los montos no cuadran: aplicaste %s a CxC y %s a CxP. "
                    + "Las sumas deben ser iguales (el cruce se hace hasta el mínimo común).",
                    totalCxc.toPlainString(), totalCxp.toPlainString()));
        }
        BigDecimal montoCompensado = totalCxc;

        // ── 4. Residual y cuenta bancaria ──
        BigDecimal residual = (form.getSaldoResidual() == null) ? CERO : form.getSaldoResidual();
        if (residual.compareTo(CERO) < 0) {
            throw new IllegalArgumentException("El saldo residual no puede ser negativo");
        }
        CuentaBancaria cuenta = null;
        if (residual.compareTo(CERO) > 0) {
            if (form.getCuentaBancariaId() == null) {
                throw new IllegalArgumentException(
                        "Si hay saldo residual, debes seleccionar la cuenta bancaria de liquidación");
            }
            cuenta = cuentaRepository.findById(form.getCuentaBancariaId())
                    .orElseThrow(() -> new IllegalArgumentException("Cuenta bancaria no encontrada"));
            if (!cuenta.getTercero().getId().equals(terceroId)) {
                throw new IllegalArgumentException("La cuenta no pertenece a este tercero");
            }
            if (cuenta.getEstatus() != EstatusCuenta.ACTIVO) {
                throw new IllegalArgumentException("La cuenta seleccionada está inactiva");
            }
        }

        // ── 5. Encabezado ──
        Compensacion comp = new Compensacion();
        comp.setFolio(generarFolio());
        comp.setTercero(tercero);
        comp.setCuentaBancaria(cuenta);
        comp.setMontoCompensado(montoCompensado);
        comp.setSaldoResidual(residual);
        comp.setObservaciones(form.getObservaciones());
        comp.setEstatus(EstatusCompensacion.APLICADA);
        comp = compensacionRepository.save(comp);

        // ── 6. Detalle + decremento de saldos ──
        for (DetalleCompensacion detalle : detalles) {
            detalle.setCompensacion(comp);
            detalleRepository.save(detalle);

            Documento doc = detalle.getDocumento();
            doc.setSaldoPendiente(doc.getSaldoPendiente().subtract(detalle.getMontoAplicado()));
            if (doc.getSaldoPendiente().compareTo(CERO) == 0) {
                doc.setEstatus(EstatusDocumento.COMPENSADO);
            }
            documentoRepository.save(doc);
        }

        return comp;
    }

    /**
     * Cancela la compensación y revierte los saldos de los documentos involucrados.:
     * restaura el saldo de cada documento involucrado.
     */
    @Transactional
    public void cancelar(Long id) {
        Compensacion comp = buscarPorId(id);
        if (comp.getEstatus() != EstatusCompensacion.APLICADA) {
            throw new IllegalArgumentException(
                    "Solo se pueden cancelar compensaciones en estatus APLICADA");
        }

        for (DetalleCompensacion detalle : detalleRepository.findByCompensacionId(id)) {
            Documento doc = detalle.getDocumento();
            doc.setSaldoPendiente(doc.getSaldoPendiente().add(detalle.getMontoAplicado()));
            // Si el documento estaba agotado y ahora tiene saldo, vuelve a VIGENTE
            if (doc.getEstatus() == EstatusDocumento.COMPENSADO
                    && doc.getSaldoPendiente().compareTo(CERO) > 0) {
                doc.setEstatus(EstatusDocumento.VIGENTE);
            }
            documentoRepository.save(doc);
        }
        comp.setEstatus(EstatusCompensacion.CANCELADA);
    }

    private String generarFolio() {
        String prefijo = "COMP-" + LocalDate.now().getYear() + "-";
        String ultimoFolio = compensacionRepository
                .findFirstByFolioStartingWithOrderByFolioDesc(prefijo)
                .map(Compensacion::getFolio)
                .orElse(prefijo + "00000");
        int consecutivo = Integer.parseInt(ultimoFolio.substring(prefijo.length())) + 1;
        return prefijo + String.format("%05d", consecutivo);
    }
    
    public long contarPorTercero(Long terceroId) {
        return compensacionRepository.countByTerceroId(terceroId);
    }

}