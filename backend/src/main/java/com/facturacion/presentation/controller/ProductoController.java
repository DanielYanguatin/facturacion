package com.facturacion.presentation.controller;

import com.facturacion.application.ports.input.ProductoUseCase;
import com.facturacion.application.ports.input.ProductoUseCase.ActualizarProductoCommand;
import com.facturacion.application.ports.input.ProductoUseCase.RegistrarProductoCommand;
import com.facturacion.application.ports.output.ImagenContenido;
import com.facturacion.domain.model.Producto;
import com.facturacion.presentation.dto.ProductoDto.ActualizarProductoRequest;
import com.facturacion.presentation.dto.ProductoDto.CrearProductoRequest;
import com.facturacion.presentation.dto.ProductoDto.ProductoResponse;
import com.facturacion.presentation.mapper.ProductoDtoMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoUseCase productoUseCase;

    public ProductoController(ProductoUseCase productoUseCase) {
        this.productoUseCase = productoUseCase;
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listarProductos(
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "soloActivos", required = false, defaultValue = "false") boolean soloActivos
    ) {
        List<ProductoResponse> lista = productoUseCase.listarProductos(nombre, categoria, soloActivos)
                .stream()
                .map(ProductoDtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtenerPorId(@PathVariable("id") String idProducto) {
        Producto producto = productoUseCase.obtenerPorId(idProducto);
        return ResponseEntity.ok(ProductoDtoMapper.toResponse(producto));
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> registrarProducto(
            @Valid @RequestBody CrearProductoRequest request
    ) {
        RegistrarProductoCommand cmd = new RegistrarProductoCommand(
                request.nombre(),
                request.descripcion(),
                request.categoria(),
                request.precioUnitario(),
                request.cantidadDisponible()
        );
        Producto nuevo = productoUseCase.registrarProducto(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductoDtoMapper.toResponse(nuevo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizarProducto(
            @PathVariable("id") String idProducto,
            @Valid @RequestBody ActualizarProductoRequest request
    ) {
        ActualizarProductoCommand cmd = new ActualizarProductoCommand(
                request.nombre(),
                request.descripcion(),
                request.categoria(),
                request.precioUnitario(),
                request.cantidadDisponible(),
                request.estado()
        );
        Producto actualizado = productoUseCase.actualizarProducto(idProducto, cmd);
        return ResponseEntity.ok(ProductoDtoMapper.toResponse(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProductoResponse> desactivarProducto(@PathVariable("id") String idProducto) {
        Producto desactivado = productoUseCase.desactivarProducto(idProducto);
        return ResponseEntity.ok(ProductoDtoMapper.toResponse(desactivado));
    }

    @PostMapping(value = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductoResponse> subirImagen(
            @PathVariable("id") String idProducto,
            @RequestParam("imagen") MultipartFile imagen
    ) throws IOException {
        Producto actualizado = productoUseCase.subirImagen(
                idProducto,
                imagen.getBytes(),
                imagen.getOriginalFilename(),
                imagen.getContentType()
        );
        return ResponseEntity.ok(ProductoDtoMapper.toResponse(actualizado));
    }

    @GetMapping("/{id}/imagen")
    public ResponseEntity<byte[]> obtenerImagen(@PathVariable("id") String idProducto) {
        ImagenContenido imagen = productoUseCase.obtenerImagen(idProducto);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, imagen.contentType())
                .body(imagen.contenido());
    }
}
