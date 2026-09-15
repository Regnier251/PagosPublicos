package com.arely.pagospublicos.kpi.controller;

import com.arely.pagospublicos.kpi.service.KpiService;
import com.arely.pagospublicos.kpi.service.KpiService.SerieMes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class KpiController {

    private final KpiService service;

    public KpiController(KpiService service) {
        this.service = service;
    }

    @GetMapping("/kpis")
    public String index(Model model) {
        // Tarjetas
        model.addAttribute("tercerosActivos", service.tercerosActivos());
        model.addAttribute("compensacionesAplicadas", service.compensacionesAplicadas());
        model.addAttribute("archivosMesActual", service.archivosRecibidosMesActual());

        // Dona: documentos por estatus
        model.addAttribute("docsVigentes", service.documentosVigentes());
        model.addAttribute("docsCompensados", service.documentosCompensados());
        model.addAttribute("docsCancelados", service.documentosCancelados());

        // Barras: serie de 6 meses (separada en etiquetas y valores para el gráfico)
        List<SerieMes> serie = service.archivosUltimos6Meses();
        model.addAttribute("etiquetasMeses", serie.stream().map(SerieMes::etiqueta).toList());
        model.addAttribute("archivosPorMes", serie.stream().map(SerieMes::valor).toList());

        return "kpi/index";
    }
}