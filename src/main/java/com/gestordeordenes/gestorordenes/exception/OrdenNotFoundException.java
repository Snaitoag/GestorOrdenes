package com.gestordeordenes.gestorordenes.exception; // O tu paquete

public class OrdenNotFoundException extends RuntimeException {
    public OrdenNotFoundException(String message) {
        super(message);
    }
}
