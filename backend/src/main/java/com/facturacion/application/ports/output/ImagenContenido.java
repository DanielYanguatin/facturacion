package com.facturacion.application.ports.output;

public record ImagenContenido(
        byte[] contenido,
        String nombreArchivo,
        String contentType
) {
}
