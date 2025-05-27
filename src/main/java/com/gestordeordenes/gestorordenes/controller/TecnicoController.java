package com.gestordeordenes.gestorordenes.controller; // O tu paquete

import com.gestordeordenes.gestorordenes.dto.OrdenCreacionDTO;
import com.gestordeordenes.gestorordenes.dto.OrdenActualizarEstadoDTO;
import com.gestordeordenes.gestorordenes.dto.OrdenVistaDTO;
import com.gestordeordenes.gestorordenes.enums.EstadoOrden;
import com.gestordeordenes.gestorordenes.exception.OrdenNotFoundException;
import com.gestordeordenes.gestorordenes.model.Usuario; // Asegúrate que es tu entidad Usuario
import com.gestordeordenes.gestorordenes.model.Orden; // Necesario para obtener el Enum EstadoOrden
import com.gestordeordenes.gestorordenes.service.OrdenService;
import com.gestordeordenes.gestorordenes.service.UsuarioServiceImpl;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;
@Controller
@RequestMapping("/tecnico")
@PreAuthorize("hasRole('ROLE_TECNICO')")
public class TecnicoController {

    private static final Logger logger = LoggerFactory.getLogger(TecnicoController.class);

    private final OrdenService ordenService;
    private final UsuarioServiceImpl usuarioService;

    @Autowired
    public TecnicoController(OrdenService ordenService, UsuarioServiceImpl usuarioService) {
        this.ordenService = ordenService;
        this.usuarioService = usuarioService;
    }

    private void addCurrentUriToModel(Model model, HttpServletRequest request) {
        model.addAttribute("currentURI", request.getRequestURI());
    }

    @GetMapping
    public String panelTecnico(Model model,
                               @AuthenticationPrincipal Usuario tecnicoLogueado,
                               HttpServletRequest request) {
        logger.info("Accediendo al panel del técnico: {}", tecnicoLogueado.getEmail());
        addCurrentUriToModel(model, request);
        List<OrdenVistaDTO> misOrdenes = ordenService.obtenerOrdenesPorTecnico(tecnicoLogueado);
        model.addAttribute("ordenes", misOrdenes);
        model.addAttribute("tecnicoNombre", tecnicoLogueado.getNombreCompleto());
        return "tecnico/panel_tecnico";
    }

    @GetMapping("/ordenes/nueva")
    public String mostrarFormularioNuevaOrden(Model model,
                                              HttpServletRequest request,
                                              @AuthenticationPrincipal Usuario tecnicoLogueado) {
        logger.info("Técnico {} mostrando formulario para nueva orden.", tecnicoLogueado.getEmail());
        addCurrentUriToModel(model, request);
        model.addAttribute("ordenCreacionDTO", new OrdenCreacionDTO());
        return "tecnico/formulario_orden";
    }

    @PostMapping("/ordenes/crear")
    public String crearNuevaOrden(@Valid @ModelAttribute("ordenCreacionDTO") OrdenCreacionDTO ordenDTO,
                                  BindingResult result,
                                  @AuthenticationPrincipal Usuario tecnicoLogueado,
                                  Model model,
                                  RedirectAttributes redirectAttributes,
                                  HttpServletRequest request) {

        if (result.hasErrors()) {
            logger.warn("Errores de validación al crear orden por técnico {}: {}", tecnicoLogueado.getEmail(), result.getAllErrors());
            addCurrentUriToModel(model, request);
            return "tecnico/formulario_orden";
        }
        try {
            logger.info("Técnico {} intentando crear nueva orden con datos: {}", tecnicoLogueado.getEmail(), ordenDTO);
            OrdenVistaDTO nuevaOrden = ordenService.crearOrden(ordenDTO, tecnicoLogueado);
            redirectAttributes.addFlashAttribute("successMessage", "Orden N°" + nuevaOrden.getNumeroOrden() + " creada exitosamente.");
            logger.info("Orden N°{} creada exitosamente por técnico {}.", nuevaOrden.getNumeroOrden(), tecnicoLogueado.getEmail());
            return "redirect:/tecnico/ordenes/" + nuevaOrden.getId();
        } catch (Exception e) {
            logger.error("Error al crear la orden por técnico {}: {}", tecnicoLogueado.getEmail(), e.getMessage(), e);
            addCurrentUriToModel(model, request);
            model.addAttribute("errorMessage", "Error al crear la orden. Por favor, intente de nuevo.");
            return "tecnico/formulario_orden";
        }
    }

    @GetMapping("/ordenes")
    public String listarOrdenes(Model model,
                                @AuthenticationPrincipal Usuario tecnicoLogueado,
                                HttpServletRequest request) {
        logger.info("Técnico {} listando todas las órdenes.", tecnicoLogueado.getEmail());
        addCurrentUriToModel(model, request);
        List<OrdenVistaDTO> todasLasOrdenes = ordenService.obtenerTodasLasOrdenes();
        model.addAttribute("ordenes", todasLasOrdenes);
        return "tecnico/lista_ordenes";
    }

    @GetMapping("/ordenes/{id}")
    public String verDetalleOrden(@PathVariable("id") Long ordenId,
                                  Model model,
                                  HttpServletRequest request,
                                  @AuthenticationPrincipal Usuario tecnicoLogueado,
                                  RedirectAttributes redirectAttributes) { // Añadir RedirectAttributes
        logger.info("Técnico {} viendo detalle de orden ID: {}", tecnicoLogueado.getEmail(), ordenId);
        addCurrentUriToModel(model, request);

        // Necesitamos la entidad Orden para obtener el Enum EstadoOrden directamente
        // Podríamos modificar OrdenService.buscarOrdenPorIdParaTecnico para que devuelva Optional<Orden>
        // o añadir un nuevo método en OrdenService que devuelva Optional<Orden>.
        // Por ahora, asumiremos que tienes un método en OrdenService que devuelve la entidad Orden.
        // Si no, necesitarás añadirlo.
        // Ejemplo: ordenService.buscarEntidadOrdenPorId(ordenId);

        Optional<Orden> ordenEntidadOpt = ordenService.buscarEntidadOrdenPorId(ordenId); // ¡DEBES CREAR ESTE MÉTODO EN OrdenService!

        if (ordenEntidadOpt.isPresent()) {
            Orden ordenEntidad = ordenEntidadOpt.get();
            // Convertimos a DTO para la visualización principal
            model.addAttribute("orden", ordenService.convertirAOrdenVistaDTO(ordenEntidad)); // Asumiendo que convertirAOrdenVistaDTO es público o accesible

            OrdenActualizarEstadoDTO actualizarDTO = new OrdenActualizarEstadoDTO();
            actualizarDTO.setNuevoEstado(ordenEntidad.getEstado()); // Pre-poblar con el estado actual (Enum)
            actualizarDTO.setDiagnosticoTecnico(ordenEntidad.getDiagnosticoTecnico()); // Pre-poblar con diagnóstico actual

            model.addAttribute("actualizarDTO", actualizarDTO);
            model.addAttribute("estadosDisponibles", Arrays.asList(EstadoOrden.values()));
            return "tecnico/detalle_orden_tecnico";
        } else {
            logger.warn("Técnico {} intentó ver orden ID: {} pero no fue encontrada.", tecnicoLogueado.getEmail(), ordenId);
            redirectAttributes.addFlashAttribute("errorMessage", "Orden con ID " + ordenId + " no encontrada.");
            return "redirect:/tecnico/ordenes";
        }
    }

    @PostMapping("/ordenes/actualizar/{id}")
    public String actualizarOrden(@PathVariable("id") Long ordenId,
                                  @Valid @ModelAttribute("actualizarDTO") OrdenActualizarEstadoDTO actualizarDTO,
                                  BindingResult result,
                                  @AuthenticationPrincipal Usuario tecnicoLogueado,
                                  Model model,
                                  RedirectAttributes redirectAttributes,
                                  HttpServletRequest request) {

        if (result.hasErrors()) {
            logger.warn("Errores de validación al actualizar orden ID {} por técnico {}: {}", ordenId, tecnicoLogueado.getEmail(), result.getAllErrors());
            // Si hay errores, necesitamos recargar los datos de la orden para mostrar la página de detalle de nuevo
            Optional<Orden> ordenEntidadOpt = ordenService.buscarEntidadOrdenPorId(ordenId); // Usar el método que devuelve la entidad
            if (ordenEntidadOpt.isPresent()) {
                addCurrentUriToModel(model, request);
                model.addAttribute("orden", ordenService.convertirAOrdenVistaDTO(ordenEntidadOpt.get()));
                model.addAttribute("estadosDisponibles", Arrays.asList(EstadoOrden.values()));
                // El 'actualizarDTO' con errores ya está en el modelo gracias a @ModelAttribute, así que no se sobrescribe.
                return "tecnico/detalle_orden_tecnico";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Orden no encontrada al intentar actualizar.");
                return "redirect:/tecnico/ordenes";
            }
        }

        try {
            logger.info("Técnico {} actualizando orden ID {} con datos: {}", tecnicoLogueado.getEmail(), ordenId, actualizarDTO);
            ordenService.actualizarOrdenPorTecnico(ordenId, actualizarDTO, tecnicoLogueado);
            redirectAttributes.addFlashAttribute("successMessage", "Orden actualizada exitosamente.");
            logger.info("Orden ID {} actualizada exitosamente por técnico {}.", ordenId, tecnicoLogueado.getEmail());
            return "redirect:/tecnico/ordenes/" + ordenId;
        } catch (OrdenNotFoundException e) {
            logger.warn("Error al actualizar orden ID {}: {}", ordenId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tecnico/ordenes";
        } catch (SecurityException e) {
            logger.warn("Error de seguridad al actualizar orden ID {} por técnico {}: {}", ordenId, tecnicoLogueado.getEmail(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tecnico/ordenes/" + ordenId;
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar orden ID {} por técnico {}: {}", ordenId, tecnicoLogueado.getEmail(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error inesperado al actualizar la orden.");
            return "redirect:/tecnico/ordenes/" + ordenId;
        }
    }
}