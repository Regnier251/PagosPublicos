package com.arely.pagospublicos.compensacion.controller;

import com.arely.pagospublicos.compensacion.model.dto.CompensacionForm;
import com.arely.pagospublicos.compensacion.model.Compensacion;
import com.arely.pagospublicos.compensacion.service.CompensacionService;
import com.arely.pagospublicos.tercero.model.*;
import com.arely.pagospublicos.tercero.repository.DocumentoRepository;
import com.arely.pagospublicos.tercero.service.CuentaBancariaService;
import com.arely.pagospublicos.tercero.service.TerceroService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/terceros/{terceroId}/compensaciones")
public class CompensacionController {

    private final CompensacionService service;
    private final TerceroService terceroService;
    private final CuentaBancariaService cuentaService;
    private final DocumentoRepository documentoRepository;

    public CompensacionController(CompensacionService service,
                                  TerceroService terceroService,
                                  CuentaBancariaService cuentaService,
                                  DocumentoRepository documentoRepository) {
        this.service = service;
        this.terceroService = terceroService;
        this.cuentaService = cuentaService;
        this.documentoRepository = documentoRepository;
    }

    @GetMapping
    public String listar(@PathVariable Long terceroId, Model model) {
        model.addAttribute("tercero", terceroService.buscarPorId(terceroId));
        model.addAttribute("compensaciones", service.listarPorTercero(terceroId));
        return "compensacion/lista";
    }

    @GetMapping("/nueva")
    public String nueva(@PathVariable Long terceroId, Model model) {
        prepararFormulario(terceroId, CompensacionForm.conLineas(
                documentosVigentes(terceroId).size()), model);
        return "compensacion/formulario";
    }

    @PostMapping
    public String aplicar(@PathVariable Long terceroId,
                          @Valid @ModelAttribute("form") CompensacionForm form,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirect) {
        if (result.hasErrors()) {
            prepararFormulario(terceroId, form, model);
            return "compensacion/formulario";
        }
        try {
            Compensacion comp = service.aplicar(terceroId, form);
            redirect.addFlashAttribute("exito",
                    "Compensación " + comp.getFolio() + " aplicada correctamente");
            return "redirect:/terceros/" + terceroId + "/compensaciones";
        } catch (IllegalArgumentException e) {
            result.reject("errorNegocio", e.getMessage());
            prepararFormulario(terceroId, form, model);
            return "compensacion/formulario";
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long terceroId, @PathVariable Long id, Model model) {
        model.addAttribute("tercero", terceroService.buscarPorId(terceroId));
        model.addAttribute("compensacion", service.buscarPorId(id));
        model.addAttribute("lineas", service.detalleDe(id));
        return "compensacion/detalle";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long terceroId, @PathVariable Long id,
                           RedirectAttributes redirect) {
        try {
            service.cancelar(id);
            redirect.addFlashAttribute("exito", "Compensación cancelada y saldos revertidos");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/terceros/" + terceroId + "/compensaciones";
    }

    // ── helpers ──

    private List<Documento> documentosVigentes(Long terceroId) {
        return documentoRepository.findByTerceroId(terceroId, Sort.by("id")).stream()
                .filter(d -> d.getEstatus() == EstatusDocumento.VIGENTE)
                .toList();
    }

    private void prepararFormulario(Long terceroId, CompensacionForm form, Model model) {
        model.addAttribute("tercero", terceroService.buscarPorId(terceroId));
        model.addAttribute("form", form);
        model.addAttribute("documentos", documentosVigentes(terceroId));
        model.addAttribute("cuentas", cuentaService.listarPorTercero(terceroId).stream()
                .filter(c -> c.getEstatus() == EstatusCuenta.ACTIVO)
                .toList());
    }
}