package com.arely.pagospublicos.kpi.service;

import com.arely.pagospublicos.compensacion.model.EstatusCompensacion;
import com.arely.pagospublicos.compensacion.repository.CompensacionRepository;
import com.arely.pagospublicos.poliza.repository.TramitePolizaRepository;
import com.arely.pagospublicos.tercero.model.EstatusDocumento;
import com.arely.pagospublicos.tercero.model.EstatusTercero;
import com.arely.pagospublicos.tercero.repository.DocumentoRepository;
import com.arely.pagospublicos.tercero.repository.TerceroRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class KpiService {

    private static final Locale ESPANOL_MX = Locale.of("es", "MX");

    private final TerceroRepository terceroRepository;
    private final DocumentoRepository documentoRepository;
    private final CompensacionRepository compensacionRepository;
    private final TramitePolizaRepository tramitePolizaRepository;

    public KpiService(TerceroRepository terceroRepository,
                      DocumentoRepository documentoRepository,
                      CompensacionRepository compensacionRepository,
                      TramitePolizaRepository tramitePolizaRepository) {
        this.terceroRepository = terceroRepository;
        this.documentoRepository = documentoRepository;
        this.compensacionRepository = compensacionRepository;
        this.tramitePolizaRepository = tramitePolizaRepository;
    }

    public long tercerosActivos() {
        return terceroRepository.countByEstatus(EstatusTercero.ACTIVO);
    }

    public long documentosVigentes() {
        return documentoRepository.countByEstatus(EstatusDocumento.VIGENTE);
    }

    public long documentosCompensados() {
        return documentoRepository.countByEstatus(EstatusDocumento.COMPENSADO);
    }

    public long documentosCancelados() {
        return documentoRepository.countByEstatus(EstatusDocumento.CANCELADO);
    }

    public long compensacionesAplicadas() {
        return compensacionRepository.countByEstatus(EstatusCompensacion.APLICADA);
    }

    public long tramitesMesActual() {
        YearMonth actual = YearMonth.now();
        return tramitePolizaRepository.countByCreadoEnBetween(
                actual.atDay(1).atStartOfDay(),
                actual.atEndOfMonth().atTime(LocalTime.MAX));
    }

    public record SerieMes(String etiqueta, long valor) {}

    public List<SerieMes> tramitesUltimos6Meses() {
        List<SerieMes> serie = new ArrayList<>();
        YearMonth actual = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth mes = actual.minusMonths(i);
            long conteo = tramitePolizaRepository.countByCreadoEnBetween(
                    mes.atDay(1).atStartOfDay(),
                    mes.atEndOfMonth().atTime(LocalTime.MAX));
            String etiqueta = mes.getMonth().getDisplayName(TextStyle.SHORT, ESPANOL_MX)
                    + " " + mes.getYear();
            serie.add(new SerieMes(etiqueta, conteo));
        }
        return serie;
    }
}