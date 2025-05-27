package com.gestordeordenes.gestorordenes.controller;

import com.gestordeordenes.gestorordenes.dto.UsuarioEdicionDTO;
import com.gestordeordenes.gestorordenes.dto.UsuarioRegistroDTO;
import com.gestordeordenes.gestorordenes.enums.Rol;
import com.gestordeordenes.gestorordenes.exception.EmailYaExisteException;
import com.gestordeordenes.gestorordenes.exception.UsuarioNotFoundException;
import com.gestordeordenes.gestorordenes.model.Usuario;
import com.gestordeordenes.gestorordenes.service.UsuarioServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UsuarioServiceImpl usuarioService;

    @Autowired
    public AdminController(UsuarioServiceImpl usuarioService) {
        this.usuarioService = usuarioService;
    }

    private void addCurrentUriToModel(Model model, HttpServletRequest request) {
        if (request != null) {
            model.addAttribute("currentURI", request.getRequestURI());
        } else {
            logger.warn("HttpServletRequest fue null en addCurrentUriToModel para AdminController.");
        }
    }

    @GetMapping
    public String panelAdmin(Model model, HttpServletRequest request) {
        addCurrentUriToModel(model, request);
        return "admin/panel_admin";
    }

    // --- CREACIÓN DE USUARIO ---
    @GetMapping("/usuarios/nuevo")
    public String mostrarFormularioNuevoUsuario(Model model, HttpServletRequest request) {
        addCurrentUriToModel(model, request);
        model.addAttribute("usuarioForm", new UsuarioRegistroDTO()); // Nombre del objeto: "usuarioForm"
        model.addAttribute("rolesDisponibles", List.of(Rol.ROLE_ADMIN, Rol.ROLE_TECNICO));
        model.addAttribute("isEditMode", false);
        return "admin/formulario_usuario";
    }

    @PostMapping("/usuarios/crear")
    public String crearNuevoUsuario(@Valid @ModelAttribute("usuarioForm") UsuarioRegistroDTO usuarioDTO, // Nombre del objeto: "usuarioForm"
                                    BindingResult result,
                                    Model model,
                                    RedirectAttributes redirectAttributes,
                                    HttpServletRequest request) {

        if (!result.hasFieldErrors("email") && usuarioDTO.getEmail() != null && !usuarioDTO.getEmail().isEmpty() &&
                usuarioService.verificarSiEmailExiste(usuarioDTO.getEmail())) {
            result.rejectValue("email", "error.usuarioForm.email", "Este email ya está registrado."); // Código de error más específico
        }

        if (result.hasErrors()) {
            addCurrentUriToModel(model, request);
            model.addAttribute("rolesDisponibles", List.of(Rol.ROLE_ADMIN, Rol.ROLE_TECNICO));
            model.addAttribute("isEditMode", false);
            return "admin/formulario_usuario";
        }

        try {
            usuarioService.registrarNuevoUsuario(usuarioDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario '" + usuarioDTO.getEmail() + "' creado exitosamente.");
            return "redirect:/admin/usuarios";
        } catch (EmailYaExisteException e) {
            addCurrentUriToModel(model, request);
            result.rejectValue("email", "error.usuarioForm.email", e.getMessage()); // Código de error más específico
            model.addAttribute("rolesDisponibles", List.of(Rol.ROLE_ADMIN, Rol.ROLE_TECNICO));
            model.addAttribute("isEditMode", false);
            return "admin/formulario_usuario";
        } catch (Exception e) {
            logger.error("Error al crear usuario: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error inesperado al crear el usuario.");
            return "redirect:/admin/usuarios/nuevo";
        }
    }

    // --- EDICIÓN DE USUARIO ---
    @GetMapping("/usuarios/editar/{id}")
    public String mostrarFormularioEditarUsuario(@PathVariable("id") Long id,
                                                 Model model,
                                                 HttpServletRequest request,
                                                 RedirectAttributes redirectAttributes,
                                                 @AuthenticationPrincipal Usuario adminLogueado) {
        addCurrentUriToModel(model, request);
        try {
            Usuario usuarioAEditar = usuarioService.obtenerUsuarioPorId(id)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID para editar: " + id));

            if (adminLogueado != null && adminLogueado.getId().equals(id) && usuarioAEditar.getRol() == Rol.ROLE_ADMIN) {
                logger.warn("Admin {} intentó editar su propia cuenta a través del formulario de edición general.", adminLogueado.getEmail());
                redirectAttributes.addFlashAttribute("warningMessage", "Para editar tu propia cuenta, usa la sección de perfil (si existiera).");
                return "redirect:/admin/usuarios";
            }

            UsuarioEdicionDTO edicionDTO = new UsuarioEdicionDTO();
            edicionDTO.setId(usuarioAEditar.getId());
            edicionDTO.setNombreCompleto(usuarioAEditar.getNombreCompleto());
            edicionDTO.setEmail(usuarioAEditar.getEmail());
            edicionDTO.setRol(usuarioAEditar.getRol());
            edicionDTO.setActivo(usuarioAEditar.isActivo());

            model.addAttribute("usuarioForm", edicionDTO); // Nombre del objeto: "usuarioForm"
            model.addAttribute("rolesDisponibles", List.of(Rol.ROLE_ADMIN, Rol.ROLE_TECNICO));
            model.addAttribute("isEditMode", true);
            return "admin/formulario_usuario";
        } catch (UsuarioNotFoundException e) {
            logger.warn("Intento de editar usuario no encontrado. ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/usuarios";
        }
    }

    @PostMapping("/usuarios/editar")
    public String actualizarUsuarioExistente(@Valid @ModelAttribute("usuarioForm") UsuarioEdicionDTO edicionDTO, // Nombre del objeto: "usuarioForm"
                                             BindingResult result,
                                             Model model,
                                             RedirectAttributes redirectAttributes,
                                             HttpServletRequest request,
                                             @AuthenticationPrincipal Usuario adminLogueado) {

        Usuario usuarioOriginal = usuarioService.obtenerUsuarioPorId(edicionDTO.getId()).orElse(null);

        if (usuarioOriginal != null) {
            if (adminLogueado != null && adminLogueado.getId().equals(edicionDTO.getId())) {
                if (edicionDTO.getRol() != Rol.ROLE_ADMIN) {
                    result.rejectValue("rol", "error.usuarioForm.rol", "No puedes cambiar tu propio rol de administrador.");
                }
                if (!edicionDTO.isActivo()) {
                    result.rejectValue("activo", "error.usuarioForm.activo", "No puedes desactivar tu propia cuenta de administrador.");
                }
            }
            else if (usuarioOriginal.getRol() == Rol.ROLE_ADMIN && usuarioService.esUnicoAdministrador(edicionDTO.getId())) {
                if (edicionDTO.getRol() != Rol.ROLE_ADMIN) {
                    result.rejectValue("rol", "error.usuarioForm.rol", "No se puede cambiar el rol del único administrador.");
                }
                if (!edicionDTO.isActivo()) {
                    result.rejectValue("activo", "error.usuarioForm.activo", "No se puede desactivar al único administrador.");
                }
            }
        } else {
            // Si usuarioOriginal es null, significa que el ID en edicionDTO no corresponde a un usuario existente.
            // @Valid ya debería haber capturado esto si el ID es @NotNull en UsuarioEdicionDTO,
            // pero es una buena defensa.
            result.reject("error.usuarioForm", "El usuario que intenta editar no existe.");
        }

        if (edicionDTO.getNuevaPassword() != null && !edicionDTO.getNuevaPassword().trim().isEmpty()) {
            if (edicionDTO.getNuevaPassword().trim().length() < 6) {
                result.rejectValue("nuevaPassword", "error.usuarioForm.nuevaPassword", "La nueva contraseña debe tener al menos 6 caracteres.");
            }
        }

        if (result.hasErrors()) {
            addCurrentUriToModel(model, request);
            model.addAttribute("rolesDisponibles", List.of(Rol.ROLE_ADMIN, Rol.ROLE_TECNICO));
            model.addAttribute("isEditMode", true);
            // "usuarioForm" (con errores) ya está en el modelo por @ModelAttribute
            return "admin/formulario_usuario";
        }

        try {
            usuarioService.actualizarUsuario(edicionDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario '" + edicionDTO.getEmail() + "' actualizado exitosamente.");
            return "redirect:/admin/usuarios";
        } catch (UsuarioNotFoundException | IllegalArgumentException e) {
            addCurrentUriToModel(model, request);
            model.addAttribute("errorMessageGlobal", e.getMessage()); // Mostrar como error global
            model.addAttribute("rolesDisponibles", List.of(Rol.ROLE_ADMIN, Rol.ROLE_TECNICO));
            model.addAttribute("isEditMode", true);
            model.addAttribute("usuarioForm", edicionDTO); // Asegurarse de que el DTO con los datos originales (o intentados) vuelva
            return "admin/formulario_usuario";
        } catch (Exception e) {
            logger.error("Error al actualizar usuario ID {}: ", edicionDTO.getId(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error inesperado al actualizar el usuario.");
            return "redirect:/admin/usuarios/editar/" + edicionDTO.getId();
        }
    }

    // --- FIN MÉTODOS PARA EDITAR USUARIO ---

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model, HttpServletRequest request) {
        addCurrentUriToModel(model, request);
        List<Usuario> listaUsuarios = usuarioService.obtenerTodosLosUsuarios();
        model.addAttribute("usuarios", listaUsuarios);
        return "admin/lista_usuarios";
    }

    @GetMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable("id") Long id,
                                  RedirectAttributes redirectAttributes,
                                  @AuthenticationPrincipal Usuario adminLogueado) {
        // Tu lógica de eliminar está bien.
        try {
            if (adminLogueado != null && adminLogueado.getId().equals(id)) {
                logger.warn("Intento de auto-eliminación por admin: {}", adminLogueado.getEmail());
                redirectAttributes.addFlashAttribute("errorMessage", "No puedes eliminar tu propia cuenta de administrador.");
                return "redirect:/admin/usuarios";
            }
            if (usuarioService.esUnicoAdministrador(id)) {
                logger.warn("Intento de eliminar el único administrador. Usuario ID: {}", id);
                redirectAttributes.addFlashAttribute("errorMessage", "No se puede eliminar el único administrador del sistema.");
                return "redirect:/admin/usuarios";
            }
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado exitosamente.");
            logger.info("Admin {} eliminó usuario con ID: {}", adminLogueado.getEmail(), id);
        } catch (UsuarioNotFoundException e) {
            logger.warn("Intento de eliminar usuario no encontrado con ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            logger.error("Error de integridad al eliminar usuario ID {}: Puede tener dependencias (ej. órdenes).", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "No se puede eliminar el usuario. Puede tener órdenes u otras dependencias asociadas.");
        } catch (IllegalStateException e) {
            logger.warn("Error de lógica de negocio al eliminar usuario ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        catch (Exception e) {
            logger.error("Error inesperado al eliminar usuario ID {}: ", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error inesperado al eliminar el usuario.");
        }
        return "redirect:/admin/usuarios";
    }
}