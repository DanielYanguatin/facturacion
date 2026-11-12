package com.facturacion.application.ports.input;

import com.facturacion.domain.model.Cliente;

import java.util.List;
import java.util.UUID;

public interface ClienteUseCase {

    record RegistrarClienteCommand(
            String nombreCompleto,
            String ciudad,
            String correo,
            String telefono,
            String categoria
    ) {
    }

    record ActualizarClienteCommand(
            String nombreCompleto,
            String ciudad,
            String correo,
            String telefono,
            String categoria
    ) {
    }

    Cliente registrarCliente(RegistrarClienteCommand command);

    Cliente actualizarCliente(UUID idCliente, ActualizarClienteCommand command);

    Cliente obtenerPorId(UUID idCliente);

    Cliente obtenerPorCorreo(String correo);

    List<Cliente> listarClientes(String busqueda);
}
