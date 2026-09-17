package com.arely.pagospublicos.poliza.controller;

import com.arely.pagospublicos.poliza.model.TramiteForm;
import com.arely.pagospublicos.poliza.model.TramitePoliza;
import com.arely.pagospublicos.poliza.service.TramitePolizaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.nio.file.Path;
import java.net.MalformedURLException;

// import java.time.LocalDate;

@Controller
@RequestMapping("/polizas")
public class TramitePolizaController {

    private final TramitePolizaService service;

    public TramitePolizaController(TramitePolizaService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tramites", service.listarRecientes());
        model.addAttribute("form", new TramiteForm());
        return "poliza/lista";
    }

    @PostMapping
    public String recibir(@Valid @ModelAttribute("form") TramiteForm form,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("tramites", service.listarRecientes());
            return "poliza/lista";
        }
        try {
            TramitePoliza tramite = service.recibir(form);
            boolean conPoliza = tramite.getArchivoPoliza() != null;
            redirect.addFlashAttribute("exito",
                    "Trámite registrado: " + tramite.getNombreProveedorSap()
                    + (conPoliza ? " — Póliza detectada: " + tramite.getArchivoPoliza() : ""));
            if (!conPoliza) {
                redirect.addFlashAttribute("advertencia",
                        "El trámite se registró, pero NO se detectó la póliza "
                        + "(PDF que contenga «poliza» y «02») dentro del ZIP.");
            }
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/polizas";
    }

    /** Ficha del trámite: datos generales + ubicación + acceso a la póliza. */
    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        TramitePoliza tramite = service.buscarPorId(id);
        model.addAttribute("tramite", tramite);

        if (tramite.getArchivoPoliza() != null) {
            try {
                model.addAttribute("datos", service.datosPoliza(tramite));
                model.addAttribute("textoPoliza", service.textoPoliza(tramite));
            } catch (IllegalArgumentException e) {
                model.addAttribute("errorLectura", e.getMessage());
            }
        }
        return "poliza/detalle";
    }

    /**
     * Sirve el PDF de la póliza al visor nativo del navegador
     * (zoom, páginas, impresión: los pone el navegador, no nosotros).
     * "inline" = se MUESTRA en vez de descargarse.
     */
    @GetMapping("/{id}/poliza.pdf")
    public ResponseEntity<UrlResource> visualizarPoliza(@PathVariable Long id) throws MalformedURLException {
        Path ruta = service.rutaAbsolutaPoliza(service.buscarPorId(id));
        UrlResource recurso = new UrlResource(ruta.toUri());
        String nombreArchivo = java.net.URLEncoder.encode(
                ruta.getFileName().toString(), java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename*=UTF-8''" + nombreArchivo)
                .body(recurso);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String excesoDeTamano(RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "El archivo excede el límite de 25 MB permitidos");
        return "redirect:/polizas";
    }
}