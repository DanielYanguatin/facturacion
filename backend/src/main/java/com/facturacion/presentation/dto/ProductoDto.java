package com.facturacion.presentation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public class ProductoDto {

    public record CrearProductoRequest(
            @NotBlank(message = "El nombre del producto es obligatorio")
            @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
            String nombre,

            String descripcion,

            @NotBlank(message = "La categoria del producto es obligatoria")
            @Size(max = 80, message = "La categoria no puede exceder 80 caracteres")
            String categoria,

            @NotNull(message = "El precio unitario es obligatorio")
            @DecimalMin(value = "0.00", message = "El precio unitario no puede ser negativo")
            BigDecimal precioUnitario,

            @Min(value = 0, message = "La cantidad disponible no puede ser negativa")
            Integer cantidadDisponible
    ) {
    }

    public record ActualizarProductoRequest(
            @NotBlank(message = "El nombre del producto es obligatorio")
            @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
            String nombre,

            String descripcion,

            @NotBlank(message = "La categoria del producto es obligatoria")
            @Size(max = 80, message = "La categoria no puede exceder 80 caracteres")
            String categoria,

            @NotNull(message = "El precio unitario es obligatorio")
            @DecimalMin(value = "0.00", message = "El precio unitario no puede ser negativo")
            BigDecimal precioUnitario,

            @Min(value = 0, message = "La cantidad disponible no puede ser negativa")
            Integer cantidadDisponible,

            String estado
    ) {
    }

    public record ProductoResponse(
            String idProducto,
            String nombre,
            String descripcion,
            String categoria,
            BigDecimal precioUnitario,
            Integer cantidadDisponible,
            String imagenId,
            String estado,
            Instant fechaRegistro,
            Instant actualizadoEn
    ) {
    }
}
