package com.gestordeordenes.gestorordenes.enums;

public enum EstadoOrden {

    RECIBIDO("Recibido"),
    EN_DIAGNOSTICO("En Diagnóstico"),
    ESPERANDO_APROBACION("Esperando Aprobación de Presupuesto"), // Opcional
    EN_REPARACION("En Reparación"),
    REPARACION_PAUSADA("Reparación Pausada"), // Opcional
    LISTO_PARA_ENTREGA("Listo para Entrega"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado");

    private final String displayName;

    EstadoOrden(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
