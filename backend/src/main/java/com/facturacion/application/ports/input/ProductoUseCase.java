package com.facturacion.application.ports.input;

import com.facturacion.application.ports.output.ImagenContenido;
import com.facturacion.domain.model.Producto;

import java.math.BigDecimal;
import java.util.List;

public interface ProductoUseCase {

    record RegistrarProductoCommand(
            String nombre,
            String descripcion,
            String categoria,
            BigDecimal precioUnitario,
            Integer cantidadDisponible
    ) {
    }

    record ActualizarProductoCommand(
            String nombre,
            String descripcion,
            String categoria,
            BigDecimal precioUnitario,
            Integer cantidadDisponible,
            String estado
    ) {
    }

    Producto registrarProducto(RegistrarProductoCommand command);

    Producto actualizarProducto(String idProducto, ActualizarProductoCommand command);

    Producto desactivarProducto(String idProducto);

    Producto obtenerPorId(String idProducto);

    List<Producto> listarProductos(String nombre, String categoria, boolean soloActivos);

    Producto subirImagen(String idProducto, byte[] contenido, String nombreArchivo, String contentType);

    ImagenContenido obtenerImagen(String idProducto);
}
