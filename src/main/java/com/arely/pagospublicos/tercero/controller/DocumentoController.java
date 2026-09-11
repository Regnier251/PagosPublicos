package com.arely.pagospublicos.tercero.controller;

import com.arely.pagospublicos.tercero.model.Documento;
import com.arely.pagospublicos.tercero.model.TipoDocumento;
import com.arely.pagospublicos.tercero.service.DocumentoService;
import com.arely.pagospublicos.tercero.service.TerceroService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/terceros/{terceroId}/documentos")
public class DocumentoController {

    private final DocumentoService service;
    private final TerceroService terceroService;

    public DocumentoController(DocumentoService service,
                               TerceroService terceroService) {
        this.service = service;
        this.terceroService = terceroService;
    }

    @GetMapping
    public String listar(@PathVariable Long terceroId, Model model) {
        model.addAttribute("tercero", terceroService.buscarPorId(terceroId));
        model.addAttribute("documentos", service.listarPorTercero(terceroId));
        return "tercero/documentos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@PathVariable Long terceroId, Model model) {
        model.addAttribute("tercero", terceroService.buscarPorId(terceroId));
        model.addAttribute("documento", new Documento());
        model.addAttribute("tipos", TipoDocumento.values());
        return "tercero/documentos/formulario";
    }

    @PostMapping
    public String guardar(@PathVariable Long terceroId,
                          @Valid @ModelAttribute("documento") Documento documento,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("tercero", terceroService.buscarPorId(terceroId));
            model.addAttribute("tipos", TipoDocumento.values());
            return "tercero/documentos/formulario";
        }
        try {
            service.guardar(terceroId, documento);
        } catch (IllegalArgumentException e) {
            model.addAttribute("tercero", terceroService.buscarPorId(terceroId));
            model.addAttribute("tipos", TipoDocumento.values());
            result.reject("errorNegocio", e.getMessage());
            return "tercero/documentos/formulario";
        }
        redirect.addFlashAttribute("exito", "Documento guardado correctamente");
        return "redirect:/terceros/" + terceroId + "/documentos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long terceroId, @PathVariable Long id, Model model) {
        model.addAttribute("tercero", terceroService.buscarPorId(terceroId));
        model.addAttribute("documento", service.buscarPorId(id));
        model.addAttribute("tipos", TipoDocumento.values());
        return "tercero/documentos/formulario";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long terceroId, @PathVariable Long id,
                           RedirectAttributes redirect) {
        try {
            service.cancelar(id);
            redirect.addFlashAttribute("exito", "Documento cancelado");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/terceros/" + terceroId + "/documentos";
    }
}