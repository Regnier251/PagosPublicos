package com.arely.pagospublicos.poliza.service;

import com.arely.pagospublicos.poliza.model.EstatusProveedor;
import com.arely.pagospublicos.poliza.model.ProveedorSap;
import com.arely.pagospublicos.poliza.repository.ProveedorSapRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProveedorSapService {

    private final ProveedorSapRepository repository;

    public ProveedorSapService(ProveedorSapRepository repository) {
        this.repository = repository;
    }

    public List<ProveedorSap> listar() {
        return repository.findAll(Sort.by("nombreSap"));
    }

    public ProveedorSap buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado: " + id));
    }

    @Transactional
    public ProveedorSap guardar(ProveedorSap datos) {
        String nombreSap = datos.getNombreSap().strip();
        boolean duplicado = (datos.getId() == null)
                ? repository.existsByNombreSapIgnoreCase(nombreSap)
                : repository.existsByNombreSapIgnoreCaseAndIdNot(nombreSap, datos.getId());
        if (duplicado) {
            throw new IllegalArgumentException(
                    "Ya existe un proveedor con ese nombre SAP: " + nombreSap);
        }

        ProveedorSap entidad = (datos.getId() != null)
                ? buscarPorId(datos.getId())
                : new ProveedorSap();

        if (entidad.getEstatus() == null) {
            entidad.setEstatus(EstatusProveedor.ACTIVO);
        }
        entidad.setNombreSap(nombreSap);
        entidad.setRazonSocial(datos.getRazonSocial().strip());
        return repository.save(entidad);
    }

    @Transactional
    public void cambiarEstatus(Long id) {
        ProveedorSap proveedor = buscarPorId(id);
        proveedor.setEstatus(proveedor.getEstatus() == EstatusProveedor.ACTIVO
                ? EstatusProveedor.INACTIVO
                : EstatusProveedor.ACTIVO);
        repository.save(proveedor);
    }
}