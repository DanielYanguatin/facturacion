package com.facturacion.domain.repository;

import com.facturacion.domain.model.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository {
    Producto guardar(Producto producto);

    Optional<Producto> buscarPorId(String idProducto);

    List<Producto> buscar(String nombre, String categoria, boolean soloActivos);

    long contarTotal();

    long contarActivos();
}
