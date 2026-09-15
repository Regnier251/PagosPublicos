package com.arely.pagospublicos.archivo.controller;

import com.arely.pagospublicos.archivo.model.ArchivoZip;
import com.arely.pagospublicos.archivo.service.ArchivoZipService;
import com.arely.pagospublicos.archivo.service.ArchivoZipService.InfoArchivoInterno;

import java.util.List;

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

    /** Bandeja de recepción: solo el mes en curso. */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("archivos", service.listarMesActual());
        return "archivo/lista";
    }

        private static final List<String> MESES = List.of(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");

    /**
     * Buscador histórico. GET (no POST): la búsqueda queda en la URL,
     * es compartible y el botón atrás funciona de forma natural.
     */
    @GetMapping("/consultar")
    public String consultar(@RequestParam(required = false) String nombre,
                            @RequestParam(required = false) Integer mes,
                            @RequestParam(required = false) Integer anio,
                            Model model) {
        model.addAttribute("meses", MESES);
        boolean hayCriterios = (nombre != null && !nombre.isBlank()) || mes != null || anio != null;

        if (!hayCriterios) {
            model.addAttribute("sinCriterios", true);
            return "archivo/consultar";
        }
        if (mes != null && anio == null) {
            model.addAttribute("errorCriterio", "Si filtras por mes, indica también el año.");
            model.addAttribute("nombre", nombre);
            model.addAttribute("mes", mes);
            return "archivo/consultar";
        }

        model.addAttribute("archivos", service.buscar(nombre, mes, anio));
        model.addAttribute("busquedaRealizada", true);
        model.addAttribute("nombre", nombre);
        model.addAttribute("mes", mes);
        model.addAttribute("anio", anio);
        return "archivo/consultar";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        ArchivoZip archivo = service.buscarPorId(id);
        List<InfoArchivoInterno> internos = service.listarArchivosInternos(archivo);
        boolean tieneCompensacionPdf = internos.stream()
                .anyMatch(InfoArchivoInterno::esPdfCompensacion);
        model.addAttribute("archivo", archivo);
        model.addAttribute("internos", internos);
        model.addAttribute("tieneCompensacionPdf", tieneCompensacionPdf);
        return "archivo/detalle";
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

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String excesoDeTamano(RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "El archivo excede el límite de 25 MB permitidos");
        return "redirect:/archivos";
    }
}