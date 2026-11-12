package com.facturacion.domain.repository;

import com.facturacion.domain.model.Cliente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository {
    Cliente guardarNuevo(Cliente cliente);

    Cliente actualizar(Cliente cliente, String correoAnterior);

    Optional<Cliente> buscarPorId(UUID idCliente);

    Optional<Cliente> buscarPorCorreo(String correo);

    List<Cliente> listarTodos();

    long contarTotal();
}
