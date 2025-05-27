package com.gestordeordenes.gestorordenes.service;

import com.gestordeordenes.gestorordenes.model.Usuario;
import com.gestordeordenes.gestorordenes.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante para operaciones de BD

@Service // Marca esta clase como un servicio de Spring
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Autowired // Inyección de dependencias del repositorio
    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true) // Buena práctica para métodos de solo lectura
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // En nuestra aplicación, el "username" es el email
        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + username));

        // Nuestra entidad Usuario ya implementa UserDetails, así que podemos retornarla directamente.
        // Spring Security usará los métodos de UserDetails (getPassword, getAuthorities, isEnabled, etc.)
        // que definimos en la entidad Usuario.
        return usuario;
    }
}
