package com.facturacion.application.ports.output;

import java.math.BigDecimal;

public record TopProductoAnalitico(
        String idProducto,
        String nombreProducto,
        String categoriaProducto,
        long unidadesVendidas,
        BigDecimal totalVendido
) {
}
