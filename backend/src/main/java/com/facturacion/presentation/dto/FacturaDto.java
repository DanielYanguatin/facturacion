package com.facturacion.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class FacturaDto {

    public record ItemFacturaRequest(
            @NotBlank(message = "El ID del producto es obligatorio")
            String idProducto,

            @NotNull(message = "La cantidad es obligatoria")
            @Min(value = 1, message = "La cantidad debe ser mayor a cero")
            Integer cantidad
    ) {
    }

    public record CrearFacturaRequest(
            @NotBlank(message = "El ID del cliente es obligatorio")
            String idCliente,

            @NotEmpty(message = "Debe incluir al menos un producto en la factura")
            @Valid
            List<ItemFacturaRequest> items
    ) {
    }

    public record DetalleFacturaResponse(
            Long idDetalle,
            String idProducto,
            String nombreProducto,
            String categoriaProducto,
            BigDecimal precioUnitario,
            Integer cantidad,
            BigDecimal totalProducto,
            String imagenId
    ) {
    }

    public record FacturaResponse(
            Long idFactura,
            String numeroFactura,
            String idCliente,
            String clienteNombre,
            String clienteCorreo,
            String clienteCiudad,
            String clienteTelefono,
            String clienteCategoria,
            LocalDateTime fechaFactura,
            BigDecimal subtotal,
            BigDecimal total,
            String metodoPago,
            String estado,
            List<DetalleFacturaResponse> detalles
    ) {
    }
}
