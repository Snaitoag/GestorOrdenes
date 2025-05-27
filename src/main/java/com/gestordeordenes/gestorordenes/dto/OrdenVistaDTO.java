package com.gestordeordenes.gestorordenes.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrdenVistaDTO {
    private Long id;
    private String numeroOrden;

    // Info Cliente
    private String clienteNombre;
    private String clienteEmail;
    private String clienteTelefono;

    // Info Dispositivo (Aplanada desde el objeto Dispositivo)
    private String dispositivoTipo;
    private String dispositivoMarca;
    private String dispositivoModelo;
    private String dispositivoSerial;

    // Info Orden
    private String problemaDescripcion;
    private String observacionesRecepcion;
    private String diagnosticoTecnico; // Opcional

    // Estado y Fechas
    private String estadoDisplayName;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacionEstado;

    // Info Técnico
    private String tecnicoAsignadoNombre;
}