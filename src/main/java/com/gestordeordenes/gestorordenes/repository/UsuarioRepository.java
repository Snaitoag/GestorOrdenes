package com.gestordeordenes.gestorordenes.repository;

import com.gestordeordenes.gestorordenes.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Esta anotación va ANTES de la declaración de la interfaz
public interface UsuarioRepository extends JpaRepository<Usuario, Long> { // SOLO UNA declaración de interfaz

    // Métodos personalizados (Spring Data JPA los implementará)
    Optional<Usuario> findByEmail(String email);


    boolean existsByEmail(String email);

    // No necesitas escribir aquí los métodos CRUD básicos como save(), findById(), etc.
    // Ya vienen heredados de JpaRepository.
}
