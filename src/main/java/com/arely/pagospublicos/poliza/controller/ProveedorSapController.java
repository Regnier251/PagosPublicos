package com.arely.pagospublicos.poliza.controller;

import com.arely.pagospublicos.poliza.model.ProveedorSap;
import com.arely.pagospublicos.poliza.service.ProveedorSapService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/proveedores")
public class ProveedorSapController {

    private final ProveedorSapService service;

    public ProveedorSapController(ProveedorSapService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("proveedores", service.listar());
        return "poliza/proveedores/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("proveedor", new ProveedorSap());
        return "poliza/proveedores/formulario";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("proveedor") ProveedorSap proveedor,
                          BindingResult result,
                          RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "poliza/proveedores/formulario";
        }
        try {
            service.guardar(proveedor);
        } catch (IllegalArgumentException e) {
            result.rejectValue("nombreSap", "duplicado", e.getMessage());
            return "poliza/proveedores/formulario";
        }
        redirect.addFlashAttribute("exito", "Proveedor guardado correctamente");
        return "redirect:/proveedores";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("proveedor", service.buscarPorId(id));
        return "poliza/proveedores/formulario";
    }

    @PostMapping("/{id}/cambiar-estatus")
    public String cambiarEstatus(@PathVariable Long id, RedirectAttributes redirect) {
        service.cambiarEstatus(id);
        redirect.addFlashAttribute("exito", "Estatus actualizado");
        return "redirect:/proveedores";
    }
}