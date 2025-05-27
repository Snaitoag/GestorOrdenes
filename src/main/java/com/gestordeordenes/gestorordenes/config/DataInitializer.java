package com.gestordeordenes.gestorordenes.config;

import com.gestordeordenes.gestorordenes.enums.Rol;
import com.gestordeordenes.gestorordenes.model.Usuario;
import com.gestordeordenes.gestorordenes.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component // Para que Spring lo detecte y gestione como un bean
public class DataInitializer implements CommandLineRunner {


    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Iniciando la inicialización de datos...");

        // Verificar si ya existe un usuario administrador para no crearlo múltiples veces
        if (!usuarioRepository.existsByEmail("admin@tutienda.com")) {
            Usuario admin = new Usuario();
            admin.setNombreCompleto("Administrador del Sistema");
            admin.setEmail("admin@tutienda.com");
            // ¡IMPORTANTE! Hashear la contraseña antes de guardarla
            admin.setPassword(passwordEncoder.encode("admin123")); // Cambia esta contraseña por una más segura
            admin.setRol(Rol.ROLE_ADMIN);
            admin.setActivo(true);
            // Las fechas de creación/actualización se manejan con @PrePersist/@PreUpdate en la entidad

            usuarioRepository.save(admin);
            logger.info("Usuario administrador creado: admin@tutienda.com");
        } else {
            logger.info("El usuario administrador 'admin@tutienda.com' ya existe.");
        }

        // Aquí podrías añadir la creación de otros datos iniciales si es necesario
        // Por ejemplo, un técnico de prueba:
        /*
        if (!usuarioRepository.existsByEmail("tecnico@tutienda.com")) {
            Usuario tecnico = new Usuario();
            tecnico.setNombreCompleto("Técnico de Prueba");
            tecnico.setEmail("tecnico@tutienda.com");
            tecnico.setPassword(passwordEncoder.encode("tecnico123"));
            tecnico.setRol(Rol.ROLE_TECNICO);
            tecnico.setActivo(true);
            usuarioRepository.save(tecnico);
            logger.info("Usuario técnico de prueba creado: tecnico@tutienda.com");
        } else {
            logger.info("El usuario técnico de prueba 'tecnico@tutienda.com' ya existe.");
        }
        */

        logger.info("Inicialización de datos completada.");
    }
}
