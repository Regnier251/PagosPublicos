package com.arely.pagospublicos.archivo.controller;

import com.arely.pagospublicos.archivo.model.ArchivoZip;
import com.arely.pagospublicos.archivo.service.ArchivoZipService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/archivos")
public class ArchivoZipController {

    private final ArchivoZipService service;

    public ArchivoZipController(ArchivoZipService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("archivos", service.listar());
        return "archivo/lista";
    }

    @PostMapping("/subir")
    public String subir(@RequestParam("archivo") MultipartFile archivo,
                        RedirectAttributes redirect) {
        try {
            ArchivoZip guardado = service.subir(archivo);
            redirect.addFlashAttribute("exito",
                    "Archivo «" + guardado.getNombreOriginal() + "» recibido con "
                            + guardado.getTotalArchivosInternos() + " archivos internos");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/archivos";
    }

    /** El límite de 25MB lo aplica Spring ANTES de entrar al método; lo capturamos aquí. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String excesoDeTamano(RedirectAttributes redirect) {
        redirect.addFlashAttribute("error",
                "El archivo excede el límite de 25 MB permitidos");
        return "redirect:/archivos";
    }
}