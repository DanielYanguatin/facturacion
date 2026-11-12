package com.facturacion.domain.model;

import com.facturacion.domain.exception.ValidacionDominioException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DetalleFactura {

    private Long idDetalle;
    private Long idFactura;
    private String idProducto;
    private String nombreProductoSnapshot;
    private String categoriaProductoSnapshot;
    private BigDecimal precioUnitario;
    private Integer cantidad;
    private BigDecimal totalProducto;

    public DetalleFactura() {
    }

    public static DetalleFactura crear(
            String idProducto,
            String nombreProductoSnapshot,
            String categoriaProductoSnapshot,
            BigDecimal precioUnitario,
            Integer cantidad
    ) {
        if (idProducto == null || idProducto.trim().isEmpty()) {
            throw new ValidacionDominioException("El ID del producto es obligatorio en el detalle.");
        }
        if (nombreProductoSnapshot == null || nombreProductoSnapshot.trim().isEmpty()) {
            throw new ValidacionDominioException("El nombre del producto es obligatorio en el detalle.");
        }
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionDominioException("El precio unitario del detalle no puede ser negativo.");
        }
        if (cantidad == null || cantidad <= 0) {
            throw new ValidacionDominioException("La cantidad del producto debe ser mayor que cero.");
        }

        BigDecimal precioNormalizado = precioUnitario.setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = precioNormalizado
                .multiply(BigDecimal.valueOf(cantidad))
                .setScale(2, RoundingMode.HALF_UP);

        DetalleFactura detalle = new DetalleFactura();
        detalle.idProducto = idProducto.trim();
        detalle.nombreProductoSnapshot = nombreProductoSnapshot.trim();
        detalle.categoriaProductoSnapshot = categoriaProductoSnapshot == null ? null : categoriaProductoSnapshot.trim();
        detalle.precioUnitario = precioNormalizado;
        detalle.cantidad = cantidad;
        detalle.totalProducto = total;
        return detalle;
    }

    public static DetalleFactura reconstruir(
            Long idDetalle,
            Long idFactura,
            String idProducto,
            String nombreProductoSnapshot,
            String categoriaProductoSnapshot,
            BigDecimal precioUnitario,
            Integer cantidad,
            BigDecimal totalProducto
    ) {
        DetalleFactura detalle = new DetalleFactura();
        detalle.idDetalle = idDetalle;
        detalle.idFactura = idFactura;
        detalle.idProducto = idProducto;
        detalle.nombreProductoSnapshot = nombreProductoSnapshot;
        detalle.categoriaProductoSnapshot = categoriaProductoSnapshot;
        detalle.precioUnitario = precioUnitario;
        detalle.cantidad = cantidad;
        detalle.totalProducto = totalProducto;
        return detalle;
    }

    public Long getIdDetalle() {
        return idDetalle;
    }

    public Long getIdFactura() {
        return idFactura;
    }

    public String getIdProducto() {
        return idProducto;
    }

    public String getNombreProductoSnapshot() {
        return nombreProductoSnapshot;
    }

    public String getCategoriaProductoSnapshot() {
        return categoriaProductoSnapshot;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public BigDecimal getTotalProducto() {
        return totalProducto;
    }
}
