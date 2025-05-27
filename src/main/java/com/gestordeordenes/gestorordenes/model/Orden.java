package com.gestordeordenes.gestorordenes.model;

import com.gestordeordenes.gestorordenes.enums.EstadoOrden;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ordenes")
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orden_id")
    private Long id;

    @Column(name = "numero_orden", unique = true, nullable = false, updatable = false)
    private String numeroOrden;

    // --- Información del Cliente (Embebida) ---
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Column(name = "cliente_nombre", nullable = false)
    private String clienteNombre;

    @Email(message = "Debe ser una dirección de email válida para el cliente")
    @Column(name = "cliente_email")
    private String clienteEmail;

    @NotBlank(message = "El teléfono del cliente es obligatorio")
    @Size(min = 7, max = 20, message = "El teléfono debe tener entre 7 y 20 caracteres")
    @Column(name = "cliente_telefono", nullable = false)
    private String clienteTelefono;

    // --- Dispositivo Asociado ---
    @NotNull(message = "El dispositivo es obligatorio para la orden")
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST) // Cascade.PERSIST para guardar nuevo dispositivo con la orden
    @JoinColumn(name = "dispositivo_id", nullable = false)
    private Dispositivo dispositivo;

    // --- Descripción del Problema y Diagnóstico ---
    @NotBlank(message = "La descripción del problema es obligatoria")
    @Lob
    @Column(name = "problema_descripcion", nullable = false, columnDefinition = "TEXT")
    private String problemaDescripcion;

    @Lob
    @Column(name = "observaciones_recepcion", columnDefinition = "TEXT")
    private String observacionesRecepcion;

    @Lob
    @Column(name = "diagnostico_tecnico", columnDefinition = "TEXT") // Notas del técnico / trabajo realizado
    private String diagnosticoTecnico;

    // --- Estado y Fechas ---
    @NotNull(message = "El estado de la orden es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_orden", nullable = false)
    private EstadoOrden estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion_estado")
    private LocalDateTime fechaActualizacionEstado;

    // --- Usuario Técnico Asociado ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnicoAsignado;

    // --- Callbacks JPA y Métodos ---
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacionEstado = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoOrden.RECIBIDO;
        }
        if (this.numeroOrden == null) {
            this.numeroOrden = generarNumeroOrden();
        }
    }

    public void cambiarEstado(EstadoOrden nuevoEstado) {
        this.estado = nuevoEstado;
        this.fechaActualizacionEstado = LocalDateTime.now();
    }

    private String generarNumeroOrden() {
        String uuidPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + uuidPart;
    }
}
