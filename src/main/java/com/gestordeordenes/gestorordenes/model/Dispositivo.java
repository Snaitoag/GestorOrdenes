package com.gestordeordenes.gestorordenes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dispositivos")
public class Dispositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dispositivo_id")
    private Long id;

    @NotBlank(message = "El tipo de dispositivo es obligatorio")
    @Column(nullable = false)
    private String tipo; // Ej: Laptop, Desktop, Smartphone

    private String marca;
    private String modelo;

    @Column(unique = true) // El serial debería ser único si se proporciona
    private String serial;

    // Podríamos añadir una relación @OneToMany a Orden si quisiéramos ver todas las órdenes de un dispositivo,
    // pero no es estrictamente necesario para la funcionalidad principal de la Orden.
}
