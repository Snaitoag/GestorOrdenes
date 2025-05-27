package com.gestordeordenes.gestorordenes.service;

import com.gestordeordenes.gestorordenes.dto.UsuarioEdicionDTO; // IMPORTAR DTO de edición
import com.gestordeordenes.gestorordenes.dto.UsuarioRegistroDTO;
import com.gestordeordenes.gestorordenes.enums.Rol;
import com.gestordeordenes.gestorordenes.exception.EmailYaExisteException;
import com.gestordeordenes.gestorordenes.model.Usuario;
import com.gestordeordenes.gestorordenes.repository.OrdenRepository;
import com.gestordeordenes.gestorordenes.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gestordeordenes.gestorordenes.exception.UsuarioNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
// Eliminado import java.util.stream.Collectors; si no se usa en otro lado

@Service
public class UsuarioServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrdenRepository ordenRepository;

    @Autowired
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              PasswordEncoder passwordEncoder,
                              @Lazy OrdenRepository ordenRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.ordenRepository = ordenRepository;
    }

    @Transactional
    public Usuario registrarNuevoUsuario(UsuarioRegistroDTO registroDTO) throws EmailYaExisteException {
        if (usuarioRepository.existsByEmail(registroDTO.getEmail())) {
            logger.warn("Intento de registrar usuario con email ya existente: {}", registroDTO.getEmail());
            throw new EmailYaExisteException("Ya existe una cuenta registrada con el email: " + registroDTO.getEmail());
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombreCompleto(registroDTO.getNombreCompleto());
        nuevoUsuario.setEmail(registroDTO.getEmail());
        nuevoUsuario.setPassword(passwordEncoder.encode(registroDTO.getPassword()));
        nuevoUsuario.setRol(registroDTO.getRol());
        nuevoUsuario.setActivo(registroDTO.isActivo());

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
        logger.info("Nuevo usuario registrado: {} con rol {}", usuarioGuardado.getEmail(), usuarioGuardado.getRol());
        return usuarioGuardado;
    }

    // --- NUEVO MÉTODO PARA ACTUALIZAR USUARIO ---
    @Transactional
    public Usuario actualizarUsuario(UsuarioEdicionDTO edicionDTO) throws UsuarioNotFoundException, EmailYaExisteException, IllegalArgumentException {
        Usuario usuarioExistente = usuarioRepository.findById(edicionDTO.getId())
                .orElseThrow(() -> {
                    logger.warn("Intento de actualizar usuario no encontrado con ID: {}", edicionDTO.getId());
                    return new UsuarioNotFoundException("Usuario no encontrado con ID: " + edicionDTO.getId());
                });

        logger.info("Actualizando usuario: ID={}, Email actual={}", usuarioExistente.getId(), usuarioExistente.getEmail());

        // El email NO se actualiza desde este DTO para mantener la simplicidad
        // Si se quisiera permitir, se necesitaría una lógica más compleja para la unicidad
        // como se comentó anteriormente. Por ahora, el DTO y el formulario deberían tener el email
        // como solo lectura o no permitir su modificación directa aquí.
        // Si el edicionDTO.getEmail() fuera diferente al usuarioExistente.getEmail(), aquí se podría validar:
        // if (!usuarioExistente.getEmail().equalsIgnoreCase(edicionDTO.getEmail())) {
        //     if (usuarioRepository.existsByEmail(edicionDTO.getEmail())) {
        //         throw new EmailYaExisteException("El nuevo email " + edicionDTO.getEmail() + " ya está en uso.");
        //     }
        //     usuarioExistente.setEmail(edicionDTO.getEmail());
        // }


        usuarioExistente.setNombreCompleto(edicionDTO.getNombreCompleto());
        usuarioExistente.setRol(edicionDTO.getRol());
        usuarioExistente.setActivo(edicionDTO.isActivo());

        // Actualizar contraseña solo si se proporcionó una nueva y no está vacía
        if (edicionDTO.getNuevaPassword() != null && !edicionDTO.getNuevaPassword().trim().isEmpty()) {
            String nuevaPasswordTrimmed = edicionDTO.getNuevaPassword().trim();
            if (nuevaPasswordTrimmed.length() < 6) {
                // Esta validación también está en el DTO con @Size, pero una doble verificación aquí no hace daño
                // o se podría confiar solo en la validación del DTO manejada por @Valid en el controlador.
                // Lanzar una excepción aquí es una forma de manejarlo si la validación del DTO falla o no se usa.
                logger.warn("Intento de establecer una nueva contraseña demasiado corta para el usuario ID {}", usuarioExistente.getId());
                throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres.");
            }
            usuarioExistente.setPassword(passwordEncoder.encode(nuevaPasswordTrimmed));
            logger.info("Contraseña del usuario ID {} actualizada.", usuarioExistente.getId());
        }

        // La fecha de actualización se maneja por @PreUpdate en la entidad Usuario
        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
        logger.info("Usuario ID {} ({}) actualizado exitosamente.", usuarioActualizado.getId(), usuarioActualizado.getEmail());
        return usuarioActualizado;
    }
    // --- FIN NUEVO MÉTODO ---


    @Transactional(readOnly = true)
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean verificarSiEmailExiste(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Transactional
    public void eliminarUsuario(Long id) throws UsuarioNotFoundException, IllegalStateException {
        logger.info("Intentando eliminar usuario con ID: {}", id);
        Usuario usuarioAEliminar = usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Intento de eliminar usuario no encontrado con ID: {}", id);
                    return new UsuarioNotFoundException("No se encontró el usuario con ID: " + id);
                });

        if (usuarioAEliminar.getRol() == Rol.ROLE_TECNICO) {
            logger.debug("El usuario {} es un técnico. Verificando órdenes asignadas...", usuarioAEliminar.getEmail());
            long countOrdenesAsignadas = ordenRepository.findByTecnicoAsignado(usuarioAEliminar).size();
            if (countOrdenesAsignadas > 0) {
                logger.warn("No se puede eliminar el técnico {} porque tiene {} órdenes asignadas.", usuarioAEliminar.getEmail(), countOrdenesAsignadas);
                throw new IllegalStateException("No se puede eliminar el técnico porque tiene órdenes asignadas. Reasígnelas primero.");
            }
            logger.debug("El técnico {} no tiene órdenes asignadas o ya fueron manejadas.", usuarioAEliminar.getEmail());
        }

        // La lógica para prevenir eliminar el último admin se manejará en el controlador
        // basado en el resultado de esUnicoAdministrador(id) ANTES de llamar a este método.

        usuarioRepository.delete(usuarioAEliminar);
        logger.info("Usuario {} (ID: {}) eliminado exitosamente.", usuarioAEliminar.getEmail(), id);
    }

    @Transactional(readOnly = true)
    public boolean esUnicoAdministrador(Long idUsuarioVerificar) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuarioVerificar);
        if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != Rol.ROLE_ADMIN) {
            return false;
        }

        List<Usuario> todosLosUsuarios = usuarioRepository.findAll();
        long contadorAdmins = todosLosUsuarios.stream()
                .filter(u -> u.getRol() == Rol.ROLE_ADMIN)
                .count();

        return contadorAdmins <= 1;
    }
}