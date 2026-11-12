package com.facturacion.presentation.controller;

import com.facturacion.application.ports.input.ClienteUseCase;
import com.facturacion.application.ports.input.ClienteUseCase.ActualizarClienteCommand;
import com.facturacion.application.ports.input.ClienteUseCase.RegistrarClienteCommand;
import com.facturacion.domain.model.Cliente;
import com.facturacion.presentation.dto.ClienteDto.ActualizarClienteRequest;
import com.facturacion.presentation.dto.ClienteDto.ClienteResponse;
import com.facturacion.presentation.dto.ClienteDto.CrearClienteRequest;
import com.facturacion.presentation.mapper.ClienteDtoMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteUseCase clienteUseCase;

    public ClienteController(ClienteUseCase clienteUseCase) {
        this.clienteUseCase = clienteUseCase;
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listarClientes(
            @RequestParam(name = "busqueda", required = false) String busqueda
    ) {
        List<ClienteResponse> lista = clienteUseCase.listarClientes(busqueda)
                .stream()
                .map(ClienteDtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtenerPorId(@PathVariable("id") UUID idCliente) {
        Cliente cliente = clienteUseCase.obtenerPorId(idCliente);
        return ResponseEntity.ok(ClienteDtoMapper.toResponse(cliente));
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> registrarCliente(
            @Valid @RequestBody CrearClienteRequest request
    ) {
        RegistrarClienteCommand cmd = new RegistrarClienteCommand(
                request.nombreCompleto(),
                request.ciudad(),
                request.correo(),
                request.telefono(),
                request.categoria()
        );
        Cliente nuevo = clienteUseCase.registrarCliente(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(ClienteDtoMapper.toResponse(nuevo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizarCliente(
            @PathVariable("id") UUID idCliente,
            @Valid @RequestBody ActualizarClienteRequest request
    ) {
        ActualizarClienteCommand cmd = new ActualizarClienteCommand(
                request.nombreCompleto(),
                request.ciudad(),
                request.correo(),
                request.telefono(),
                request.categoria()
        );
        Cliente actualizado = clienteUseCase.actualizarCliente(idCliente, cmd);
        return ResponseEntity.ok(ClienteDtoMapper.toResponse(actualizado));
    }
}
