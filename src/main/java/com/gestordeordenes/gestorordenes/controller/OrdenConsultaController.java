package com.gestordeordenes.gestorordenes.controller; // O tu paquete

import com.gestordeordenes.gestorordenes.dto.OrdenVistaDTO;
import com.gestordeordenes.gestorordenes.service.OrdenService;
import jakarta.servlet.http.HttpServletRequest; // IMPORTAR HttpServletRequest
import org.slf4j.Logger; // IMPORTAR Logger (opcional, pero bueno para trazas)
import org.slf4j.LoggerFactory; // IMPORTAR LoggerFactory (opcional)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class OrdenConsultaController {

    private static final Logger logger = LoggerFactory.getLogger(OrdenConsultaController.class); // Logger opcional

    private final OrdenService ordenService;

    @Autowired
    public OrdenConsultaController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    // Método helper (puedes copiarlo de AppController o tenerlo aquí también)
    private void addCurrentUriToModel(Model model, HttpServletRequest request) {
        if (request != null) {
            model.addAttribute("currentURI", request.getRequestURI());
        } else {
            logger.warn("HttpServletRequest fue null en addCurrentUriToModel para OrdenConsultaController.");
        }
    }

    @GetMapping("/orden/buscar")
    public String buscarOrdenPorNumero(@RequestParam(name = "numeroOrden", required = false) String numeroOrden,
                                       Model model,
                                       RedirectAttributes redirectAttributes,
                                       HttpServletRequest request) { // AÑADIR HttpServletRequest request

        // Llamar a addCurrentUriToModel al principio si múltiples retornos sirven plantillas directamente
        addCurrentUriToModel(model, request); // AÑADIR esta línea

        if (numeroOrden == null || numeroOrden.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorConsulta", "Por favor, ingrese un número de orden.");
            logger.info("Intento de búsqueda de orden con número vacío.");
            return "redirect:/";
        }

        String numeroOrdenTrimmed = numeroOrden.trim().toUpperCase(); // NORMALIZAR a mayúsculas para consistencia con la generación de número de orden
        logger.info("Buscando orden con número: {}", numeroOrdenTrimmed);
        Optional<OrdenVistaDTO> ordenOpt = ordenService.buscarOrdenPorNumeroParaCliente(numeroOrdenTrimmed);

        if (ordenOpt.isPresent()) {
            logger.info("Orden encontrada: {}", numeroOrdenTrimmed);
            // addCurrentUriToModel(model, request); // Ya se llamó arriba
            model.addAttribute("orden", ordenOpt.get());
            return "public/detalle_orden_cliente";
        } else {
            logger.warn("Orden no encontrada con número: {}", numeroOrdenTrimmed);
            // addCurrentUriToModel(model, request); // Ya se llamó arriba
            model.addAttribute("numeroOrdenBuscado", numeroOrdenTrimmed);
            model.addAttribute("errorMensaje", "No se encontró ninguna orden con el número: " + numeroOrdenTrimmed);
            return "public/pagina_consulta_orden";
        }
    }
}