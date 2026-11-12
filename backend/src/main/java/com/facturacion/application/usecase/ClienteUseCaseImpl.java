package com.facturacion.application.usecase;

import com.facturacion.application.ports.input.ClienteUseCase;
import com.facturacion.domain.exception.ConflictoDominioException;
import com.facturacion.domain.exception.EntidadNoEncontradaException;
import com.facturacion.domain.model.Cliente;
import com.facturacion.domain.repository.ClienteRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ClienteUseCaseImpl implements ClienteUseCase {

    private final ClienteRepository clienteRepository;

    public ClienteUseCaseImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public Cliente registrarCliente(RegistrarClienteCommand command) {
        Cliente nuevo = Cliente.crearNuevo(
                command.nombreCompleto(),
                command.ciudad(),
                command.correo(),
                command.telefono(),
                command.categoria()
        );

        clienteRepository.buscarPorCorreo(nuevo.getCorreo()).ifPresent(existente -> {
            throw new ConflictoDominioException(
                    "Ya existe un cliente registrado con el correo '" + nuevo.getCorreo() + "'."
            );
        });

        return clienteRepository.guardarNuevo(nuevo);
    }

    @Override
    public Cliente actualizarCliente(UUID idCliente, ActualizarClienteCommand command) {
        Cliente existente = obtenerPorId(idCliente);
        String correoAnterior = existente.getCorreo();

        existente.actualizar(
                command.nombreCompleto(),
                command.ciudad(),
                command.correo(),
                command.telefono(),
                command.categoria()
        );

        if (!correoAnterior.equalsIgnoreCase(existente.getCorreo())) {
            clienteRepository.buscarPorCorreo(existente.getCorreo()).ifPresent(otro -> {
                if (!otro.getIdCliente().equals(idCliente)) {
                    throw new ConflictoDominioException(
                            "El correo '" + existente.getCorreo() + "' ya pertenece a otro cliente."
                    );
                }
            });
        }

        return clienteRepository.actualizar(existente, correoAnterior);
    }

    @Override
    public Cliente obtenerPorId(UUID idCliente) {
        return clienteRepository.buscarPorId(idCliente)
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "No se encontro el cliente con ID '" + idCliente + "'."
                ));
    }

    @Override
    public Cliente obtenerPorCorreo(String correo) {
        String normalizado = correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
        return clienteRepository.buscarPorCorreo(normalizado)
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "No se encontro un cliente con el correo '" + correo + "'."
                ));
    }

    @Override
    public List<Cliente> listarClientes(String busqueda) {
        List<Cliente> todos = clienteRepository.listarTodos();
        String filtro = busqueda == null ? "" : busqueda.trim().toLowerCase(Locale.ROOT);

        return todos.stream()
                .filter(c -> filtro.isEmpty() || coincideFiltro(c, filtro))
                .sorted(Comparator.comparing(
                        Cliente::getFechaRegistro,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();
    }

    private boolean coincideFiltro(Cliente cliente, String filtro) {
        return contiene(cliente.getNombreCompleto(), filtro)
                || contiene(cliente.getCiudad(), filtro)
                || contiene(cliente.getCorreo(), filtro)
                || contiene(cliente.getTelefono(), filtro)
                || contiene(cliente.getCategoria(), filtro)
                || contiene(cliente.getIdCliente() != null ? cliente.getIdCliente().toString() : "", filtro);
    }

    private boolean contiene(String campo, String filtro) {
        return campo != null && campo.toLowerCase(Locale.ROOT).contains(filtro);
    }
}
