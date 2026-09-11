package com.arely.pagospublicos.tercero.controller;

import com.arely.pagospublicos.tercero.model.CuentaBancaria;
import com.arely.pagospublicos.tercero.service.CuentaBancariaService;
import com.arely.pagospublicos.tercero.service.TerceroService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/terceros/{terceroId}/cuentas")
public class CuentaBancariaController {

    private final CuentaBancariaService service;
    private final TerceroService terceroService;

    public CuentaBancariaController(CuentaBancariaService service,
                                    TerceroService terceroService) {
        this.service = service;
        this.terceroService = terceroService;
    }

    @GetMapping
    public String listar(@PathVariable Long terceroId, Model model) {
        model.addAttribute("tercero", terceroService.buscarPorId(terceroId));
        model.addAttribute("cuentas", service.listarPorTercero(terceroId));
        return "tercero/cuentas/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@PathVariable Long terceroId, Model model) {
        model.addAttribute("tercero", terceroService.buscarPorId(terceroId));
        model.addAttribute("cuenta", new CuentaBancaria());
        return "tercero/cuentas/formulario";
    }

    @PostMapping
    public String guardar(@PathVariable Long terceroId,
                          @Valid @ModelAttribute("cuenta") CuentaBancaria cuenta,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("tercero", terceroService.buscarPorId(terceroId));
            return "tercero/cuentas/formulario";
        }
        try {
            service.guardar(terceroId, cuenta);
        } catch (IllegalArgumentException e) {
            model.addAttribute("tercero", terceroService.buscarPorId(terceroId));
            result.reject("errorNegocio", e.getMessage());
            return "tercero/cuentas/formulario";
        }
        redirect.addFlashAttribute("exito", "Cuenta guardada correctamente");
        return "redirect:/terceros/" + terceroId + "/cuentas";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long terceroId, @PathVariable Long id, Model model) {
        model.addAttribute("tercero", terceroService.buscarPorId(terceroId));
        model.addAttribute("cuenta", service.buscarPorId(id));
        return "tercero/cuentas/formulario";
    }

    @PostMapping("/{id}/cambiar-estatus")
    public String cambiarEstatus(@PathVariable Long terceroId, @PathVariable Long id,
                                 RedirectAttributes redirect) {
        service.cambiarEstatus(id);
        redirect.addFlashAttribute("exito", "Estatus actualizado");
        return "redirect:/terceros/" + terceroId + "/cuentas";
    }
}