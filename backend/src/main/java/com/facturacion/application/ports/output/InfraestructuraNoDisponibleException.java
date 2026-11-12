package com.facturacion.application.ports.output;

public class InfraestructuraNoDisponibleException extends RuntimeException {
    private final String motor;

    public InfraestructuraNoDisponibleException(String motor, String message, Throwable cause) {
        super(message, cause);
        this.motor = motor;
    }

    public String getMotor() {
        return motor;
    }
}
