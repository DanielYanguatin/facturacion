package com.facturacion.application.usecase;

import com.facturacion.application.ports.input.ProductoUseCase;
import com.facturacion.application.ports.output.ImagenContenido;
import com.facturacion.application.ports.output.ImagenStoragePort;
import com.facturacion.domain.exception.EntidadNoEncontradaException;
import com.facturacion.domain.exception.ValidacionDominioException;
import com.facturacion.domain.model.Producto;
import com.facturacion.domain.repository.ProductoRepository;

import java.util.List;
import java.util.Set;

public class ProductoUseCaseImpl implements ProductoUseCase {

    private static final Set<String> MIME_PERMITIDOS = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final ProductoRepository productoRepository;
    private final ImagenStoragePort imagenStoragePort;

    public ProductoUseCaseImpl(
            ProductoRepository productoRepository,
            ImagenStoragePort imagenStoragePort
    ) {
        this.productoRepository = productoRepository;
        this.imagenStoragePort = imagenStoragePort;
    }

    @Override
    public Producto registrarProducto(RegistrarProductoCommand command) {
        Producto nuevo = Producto.crearNuevo(
                command.nombre(),
                command.descripcion(),
                command.categoria(),
                command.precioUnitario(),
                command.cantidadDisponible()
        );
        return productoRepository.guardar(nuevo);
    }

    @Override
    public Producto actualizarProducto(String idProducto, ActualizarProductoCommand command) {
        Producto existente = obtenerPorId(idProducto);
        existente.actualizar(
                command.nombre(),
                command.descripcion(),
                command.categoria(),
                command.precioUnitario(),
                command.cantidadDisponible(),
                command.estado()
        );
        return productoRepository.guardar(existente);
    }

    @Override
    public Producto desactivarProducto(String idProducto) {
        Producto existente = obtenerPorId(idProducto);
        existente.desactivar();
        return productoRepository.guardar(existente);
    }

    @Override
    public Producto obtenerPorId(String idProducto) {
        return productoRepository.buscarPorId(idProducto)
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "No se encontro el producto con ID '" + idProducto + "'."
                ));
    }

    @Override
    public List<Producto> listarProductos(String nombre, String categoria, boolean soloActivos) {
        return productoRepository.buscar(nombre, categoria, soloActivos);
    }

    @Override
    public Producto subirImagen(
            String idProducto,
            byte[] contenido,
            String nombreArchivo,
            String contentType
    ) {
        if (contenido == null || contenido.length == 0) {
            throw new ValidacionDominioException("Debe proporcionar un archivo de imagen valido.");
        }
        String tipo = contentType == null ? "" : contentType.trim().toLowerCase();
        if (!MIME_PERMITIDOS.contains(tipo)) {
            throw new ValidacionDominioException(
                    "Formato de imagen no soportado ('" + contentType + "'). Use JPEG, PNG, WEBP o GIF."
            );
        }

        Producto producto = obtenerPorId(idProducto);
        String imagenAnterior = producto.getImagenId();
        String nuevaImagenId = imagenStoragePort.guardarImagen(
                contenido,
                nombreArchivo == null ? "producto.jpg" : nombreArchivo,
                tipo
        );
        producto.asignarImagen(nuevaImagenId);
        Producto actualizado = productoRepository.guardar(producto);

        if (imagenAnterior != null && !imagenAnterior.isBlank() && !imagenAnterior.equals(nuevaImagenId)) {
            imagenStoragePort.eliminarImagen(imagenAnterior);
        }
        return actualizado;
    }

    @Override
    public ImagenContenido obtenerImagen(String idProducto) {
        Producto producto = obtenerPorId(idProducto);
        if (producto.getImagenId() == null || producto.getImagenId().isBlank()) {
            throw new EntidadNoEncontradaException(
                    "El producto '" + producto.getNombre() + "' no tiene una imagen registrada."
            );
        }
        return imagenStoragePort.obtenerImagen(producto.getImagenId())
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "No se encontro la imagen asociada al producto '" + idProducto + "'."
                ));
    }
}
