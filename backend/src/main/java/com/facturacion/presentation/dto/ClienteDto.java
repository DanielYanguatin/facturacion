package com.facturacion.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public class ClienteDto {

    public record CrearClienteRequest(
            @NotBlank(message = "El nombre completo es obligatorio")
            @Size(max = 120, message = "El nombre no puede exceder 120 caracteres")
            String nombreCompleto,

            @NotBlank(message = "La ciudad es obligatoria")
            @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
            String ciudad,

            @NotBlank(message = "El correo electronico es obligatorio")
            @Email(message = "El formato del correo electronico no es valido")
            String correo,

            @NotBlank(message = "El telefono es obligatorio")
            @Size(max = 30, message = "El telefono no puede exceder 30 caracteres")
            String telefono,

            String categoria
    ) {
    }

    public record ActualizarClienteRequest(
            @NotBlank(message = "El nombre completo es obligatorio")
            @Size(max = 120, message = "El nombre no puede exceder 120 caracteres")
            String nombreCompleto,

            @NotBlank(message = "La ciudad es obligatoria")
            @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
            String ciudad,

            @NotBlank(message = "El correo electronico es obligatorio")
            @Email(message = "El formato del correo electronico no es valido")
            String correo,

            @NotBlank(message = "El telefono es obligatorio")
            @Size(max = 30, message = "El telefono no puede exceder 30 caracteres")
            String telefono,

            String categoria
    ) {
    }

    public record ClienteResponse(
            UUID idCliente,
            String nombreCompleto,
            String ciudad,
            String correo,
            String telefono,
            String categoria,
            Instant fechaRegistro,
            Instant actualizadoEn
    ) {
    }
}
