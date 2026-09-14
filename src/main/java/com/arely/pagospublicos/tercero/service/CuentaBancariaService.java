package com.arely.pagospublicos.tercero.service;

import com.arely.pagospublicos.tercero.model.CuentaBancaria;
import com.arely.pagospublicos.tercero.model.EstatusCuenta;
import com.arely.pagospublicos.tercero.model.Tercero;
import com.arely.pagospublicos.tercero.repository.CuentaBancariaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CuentaBancariaService {

    private final CuentaBancariaRepository repository;
    private final TerceroService terceroService;

    public CuentaBancariaService(CuentaBancariaRepository repository,
                                 TerceroService terceroService) {
        this.repository = repository;
        this.terceroService = terceroService;
    }

    public List<CuentaBancaria> listarPorTercero(Long terceroId) {
        return repository.findByTerceroId(terceroId, Sort.by("banco"));
    }

    public CuentaBancaria buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada: " + id));
    }

    @Transactional
    public CuentaBancaria guardar(Long terceroId, CuentaBancaria datos) {
        Tercero tercero = terceroService.buscarPorId(terceroId);

        String clabe = (datos.getClabe() == null || datos.getClabe().isBlank())
                ? null
                : datos.getClabe().trim();

        validarClabeUnica(clabe, datos.getId());

        CuentaBancaria entidad = (datos.getId() != null)
                ? buscarPorId(datos.getId())
                : new CuentaBancaria();

        // Regla de integridad: al editar, la cuenta debe seguir
        // perteneciendo al tercero de la URL
        if (datos.getId() != null && !entidad.getTercero().getId().equals(terceroId)) {
            throw new IllegalArgumentException("La cuenta no pertenece a este tercero");
        }

        if (entidad.getEstatus() == null) {
            entidad.setEstatus(EstatusCuenta.ACTIVO);
        }
        entidad.setTercero(tercero);
        entidad.setBanco(datos.getBanco().trim());
        entidad.setClabe(clabe);
        entidad.setTitular(datos.getTitular().trim());
        return repository.save(entidad);
    }

    @Transactional
    public void cambiarEstatus(Long id) {
        CuentaBancaria cuenta = buscarPorId(id);
        cuenta.setEstatus(cuenta.getEstatus() == EstatusCuenta.ACTIVO
                ? EstatusCuenta.INACTIVA
                : EstatusCuenta.ACTIVO);
        repository.save(cuenta);
    }

    private void validarClabeUnica(String clabe, Long id) {
        if (clabe == null) {
            return;
        }
        boolean duplicada = (id == null)
                ? repository.existsByClabe(clabe)
                : repository.existsByClabeAndIdNot(clabe, id);
        if (duplicada) {
            throw new IllegalArgumentException("Ya existe una cuenta registrada con esa CLABE");
        }
    }
    public long contarPorTercero(Long terceroId) {
        return repository.countByTerceroId(terceroId);
    }
}