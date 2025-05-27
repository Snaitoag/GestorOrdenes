package com.gestordeordenes.gestorordenes.dto;

import com.gestordeordenes.gestorordenes.enums.EstadoOrden;
import jakarta.validation.constraints.NotNull;
//import lombok.Data;


public class OrdenActualizarEstadoDTO {
    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoOrden nuevoEstado;

    private String diagnosticoTecnico;

    // Constructor sin argumentos (si quitaste @Data y no tienes @NoArgsConstructor)
    public OrdenActualizarEstadoDTO() {
    }

    // Getters y Setters manuales
    public EstadoOrden getNuevoEstado() {
        return nuevoEstado;
    }

    public void setNuevoEstado(EstadoOrden nuevoEstado) {
        this.nuevoEstado = nuevoEstado;
    }

    public String getDiagnosticoTecnico() {
        return diagnosticoTecnico;
    }

    public void setDiagnosticoTecnico(String diagnosticoTecnico) {
        this.diagnosticoTecnico = diagnosticoTecnico;
    }

    // Si tenías @AllArgsConstructor y lo quitaste con @Data,
    // podrías necesitar un constructor con argumentos si lo usabas en alguna parte,
    // aunque para los DTOs de formulario usualmente solo se necesita el constructor vacío.
}