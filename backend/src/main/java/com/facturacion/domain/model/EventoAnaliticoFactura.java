package com.facturacion.domain.model;

import java.math.BigDecimal;

public record EventoAnaliticoFactura(
        Long id_factura,
        String numero_factura,
        String fecha_factura,
        String id_cliente,
        String categoria_cliente,
        String id_producto,
        String nombre_producto,
        String categoria_producto,
        BigDecimal precio_unitario,
        Integer cantidad,
        BigDecimal total_producto,
        BigDecimal total_factura
) {
}
