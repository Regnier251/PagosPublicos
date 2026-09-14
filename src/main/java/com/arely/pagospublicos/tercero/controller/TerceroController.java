package com.arely.pagospublicos.tercero.controller;

import com.arely.pagospublicos.compensacion.service.CompensacionService;
import com.arely.pagospublicos.tercero.model.Tercero;
import com.arely.pagospublicos.tercero.service.CuentaBancariaService;
import com.arely.pagospublicos.tercero.service.DocumentoService;
import com.arely.pagospublicos.tercero.service.TerceroService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/terceros")
public class TerceroController {

    private final TerceroService service;
    private final CuentaBancariaService cuentaService;
    private final DocumentoService documentoService;
    private final CompensacionService compensacionService;

    public TerceroController(TerceroService service,
                             CuentaBancariaService cuentaService,
                             DocumentoService documentoService,
                             CompensacionService compensacionService) {
        this.service = service;
        this.cuentaService = cuentaService;
        this.documentoService = documentoService;
        this.compensacionService = compensacionService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("terceros", service.listar());
        return "tercero/lista";
    }

    /** Ficha del tercero: hub central con resumen y accesos. */
    @GetMapping("/{id}")
    public String verFicha(@PathVariable Long id, Model model) {
        model.addAttribute("tercero", service.buscarPorId(id));
        model.addAttribute("totalCuentas", cuentaService.contarPorTercero(id));
        model.addAttribute("totalDocumentos", documentoService.contarPorTercero(id));
        model.addAttribute("totalCompensaciones", compensacionService.contarPorTercero(id));
        return "tercero/ficha";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("tercero", new Tercero());
        return "tercero/formulario";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("tercero") Tercero tercero,
                          BindingResult result,
                          RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "tercero/formulario";
        }
        try {
            service.guardar(tercero);
        } catch (IllegalArgumentException e) {
            result.rejectValue("rfc", "duplicado", e.getMessage());
            return "tercero/formulario";
        }
        redirect.addFlashAttribute("exito", "Tercero guardado correctamente");
        return "redirect:/terceros";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("tercero", service.buscarPorId(id));
        return "tercero/formulario";
    }

    @PostMapping("/{id}/cambiar-estatus")
    public String cambiarEstatus(@PathVariable Long id, RedirectAttributes redirect) {
        service.cambiarEstatus(id);
        redirect.addFlashAttribute("exito", "Estatus actualizado");
        // El toggle ahora vive en la ficha: regresamos a ella, no a la lista
        return "redirect:/terceros/" + id;
    }
}