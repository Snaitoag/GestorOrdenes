package com.gestordeordenes.gestorordenes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrdenCreacionDTO {

    // Info Cliente
    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String clienteNombre;

    @Email(message = "El email del cliente debe ser válido")
    private String clienteEmail; // Opcional

    @NotBlank(message = "El teléfono del cliente es obligatorio")
    @Size(min = 7, max = 20, message = "El teléfono debe tener entre 7 y 20 caracteres")
    private String clienteTelefono;

    // Info Dispositivo (campos para crear un nuevo Dispositivo o seleccionar uno existente)
    @NotBlank(message = "El tipo de dispositivo es obligatorio")
    private String dispositivoTipo;

    private String dispositivoMarca;
    private String dispositivoModelo;
    private String dispositivoSerial; // Si se proporciona y existe, podríamos reutilizar el dispositivo

    // Info Orden
    @NotBlank(message = "La descripción del problema es obligatoria")
    private String problemaDescripcion;

    private String observacionesRecepcion;

    // El campo diagnosticoTecnico es opcional y se llenaría después
    // private String diagnosticoTecnico;
}