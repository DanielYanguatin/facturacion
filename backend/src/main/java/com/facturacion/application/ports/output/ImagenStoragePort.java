package com.facturacion.application.ports.output;

import java.util.Optional;

public interface ImagenStoragePort {
    String guardarImagen(byte[] contenido, String nombreArchivo, String contentType);

    Optional<ImagenContenido> obtenerImagen(String imagenId);

    void eliminarImagen(String imagenId);
}
