package com.arely.pagospublicos.tercero.service;

import com.arely.pagospublicos.tercero.model.Documento;
import com.arely.pagospublicos.tercero.model.EstatusDocumento;
import com.arely.pagospublicos.tercero.model.Tercero;
import com.arely.pagospublicos.tercero.repository.DocumentoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DocumentoService {

    private final DocumentoRepository repository;
    private final TerceroService terceroService;

    public DocumentoService(DocumentoRepository repository,
                            TerceroService terceroService) {
        this.repository = repository;
        this.terceroService = terceroService;
    }

    public List<Documento> listarPorTercero(Long terceroId) {
        return repository.findByTerceroId(terceroId,
                Sort.by("fechaEmision").descending());
    }

    public Documento buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado: " + id));
    }

    @Transactional
    public Documento guardar(Long terceroId, Documento datos) {
        Tercero tercero = terceroService.buscarPorId(terceroId);

        boolean nuevo = datos.getId() == null;
        Documento entidad = nuevo ? new Documento() : buscarPorId(datos.getId());

        // Integridad: el documento de la URL debe coincidir con el de BD
        if (!nuevo && !entidad.getTercero().getId().equals(terceroId)) {
            throw new IllegalArgumentException("El documento no pertenece a este tercero");
        }

        // Regla 1: el tipo se define al crear y no cambia nunca
        if (!nuevo && entidad.getTipo() != datos.getTipo()) {
            throw new IllegalArgumentException("El tipo de documento no se puede modificar");
        }

        // Regla 2: si el documento ya tiene montos aplicados por compensaciones,
        // su monto total queda congelado (integridad del histórico)
        boolean sinMovimientos = !nuevo
                && entidad.getSaldoPendiente().compareTo(entidad.getMontoTotal()) == 0;
        if (!nuevo && !sinMovimientos
                && datos.getMontoTotal().compareTo(entidad.getMontoTotal()) != 0) {
            throw new IllegalArgumentException(
                    "El documento ya tiene compensaciones aplicadas; el monto total no se puede modificar");
        }

        if (entidad.getEstatus() == null) {
            entidad.setEstatus(EstatusDocumento.VIGENTE);
        }
        entidad.setTercero(tercero);
        entidad.setTipo(datos.getTipo());
        entidad.setFolio(datos.getFolio().trim());
        entidad.setFechaEmision(datos.getFechaEmision());
        entidad.setFechaVencimiento(datos.getFechaVencimiento());
        entidad.setMontoTotal(datos.getMontoTotal());

        // Regla 3: el saldo lo maneja el sistema
        // - nuevo: saldo = monto total
        // - editado sin movimientos: saldo = nuevo monto
        // - editado con movimientos: intacto (y el monto no pudo cambiar)
        if (nuevo || sinMovimientos) {
            entidad.setSaldoPendiente(datos.getMontoTotal());
        }

        return repository.save(entidad);
    }

    @Transactional
    public void cancelar(Long id) {
        Documento documento = buscarPorId(id);
        if (documento.getEstatus() != EstatusDocumento.VIGENTE) {
            throw new IllegalArgumentException("Solo se pueden cancelar documentos en estatus VIGENTE");
        }
        documento.setEstatus(EstatusDocumento.CANCELADO);
        repository.save(documento);
    }
}