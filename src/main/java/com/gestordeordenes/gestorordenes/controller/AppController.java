package com.gestordeordenes.gestorordenes.controller; // O tu nombre de paquete

import jakarta.servlet.http.HttpServletRequest; // IMPORTAR HttpServletRequest
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppController {

    // Método helper para añadir la URI actual al modelo
    // Puedes decidir si pones este helper en cada controlador o creas una clase de utilidad
    // o incluso un @ControllerAdvice para añadirlo globalmente a todas las vistas.
    // Por ahora, lo mantenemos simple en cada controlador que lo necesite.
    private void addCurrentUriToModel(Model model, HttpServletRequest request) {
        if (request != null) { // Buena práctica verificar null, aunque con inyección no debería serlo
            model.addAttribute("currentURI", request.getRequestURI());
        }
    }

    @GetMapping("/")
    public String viewHomePage(Model model, HttpServletRequest request) { // AÑADIR HttpServletRequest request
        addCurrentUriToModel(model, request); // AÑADIR esta línea
        return "public/pagina_consulta_orden";
    }

    @GetMapping("/login")
    public String loginPage(Model model, HttpServletRequest request) { // AÑADIR HttpServletRequest request Y Model model
        // Asumimos que la página de login también podría tener una estructura de navegación
        // que se beneficie de currentURI. Si no, estas adiciones son inocuas.
        addCurrentUriToModel(model, request); // AÑADIR esta línea
        return "login";
    }

    /*
    // Ejemplo si tuvieras un admin dashboard servido desde aquí:
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, HttpServletRequest request) { // AÑADIR HttpServletRequest request
        addCurrentUriToModel(model, request); // AÑADIR esta línea
        return "admin/dashboard_admin";
    }
    */
}