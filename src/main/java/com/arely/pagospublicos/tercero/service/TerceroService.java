package com.arely.pagospublicos.tercero.service;

import com.arely.pagospublicos.tercero.model.EstatusTercero;
import com.arely.pagospublicos.tercero.model.Tercero;
import com.arely.pagospublicos.tercero.repository.TerceroRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TerceroService {

    private final TerceroRepository repository;

    public TerceroService(TerceroRepository repository) {
        this.repository = repository;
    }

    public List<Tercero> listar() {
        return repository.findAll(Sort.by("razonSocial"));
    }

    public Tercero buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tercero no encontrado: " + id));
    }

    @Transactional
    public Tercero guardar(Tercero datos) {
        String rfc = datos.getRfc().trim().toUpperCase();

        validarRfcUnico(rfc, datos.getId());

        Tercero entidad = (datos.getId() != null)
                ? buscarPorId(datos.getId())
                : new Tercero();

        if (entidad.getEstatus() == null) {
            entidad.setEstatus(EstatusTercero.ACTIVO);
        }
        entidad.setRfc(rfc);
        entidad.setRazonSocial(datos.getRazonSocial().trim());
        return repository.save(entidad);
    }

    @Transactional
    public void cambiarEstatus(Long id) {
        Tercero tercero = buscarPorId(id);
        tercero.setEstatus(tercero.getEstatus() == EstatusTercero.ACTIVO
                ? EstatusTercero.INACTIVO
                : EstatusTercero.ACTIVO);
        repository.save(tercero);
    }

    private void validarRfcUnico(String rfc, Long id) {
        boolean duplicado = (id == null)
                ? repository.existsByRfc(rfc)
                : repository.existsByRfcAndIdNot(rfc, id);
        if (duplicado) {
            throw new IllegalArgumentException("Ya existe un tercero con el RFC " + rfc);
        }
    }
}