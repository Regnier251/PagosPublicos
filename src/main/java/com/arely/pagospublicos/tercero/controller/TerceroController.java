package com.arely.pagospublicos.tercero.controller;

import com.arely.pagospublicos.tercero.model.Tercero;
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

    public TerceroController(TerceroService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("terceros", service.listar());
        return "tercero/lista";
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
        return "redirect:/terceros";
    }
}