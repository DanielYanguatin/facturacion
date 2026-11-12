package com.facturacion.presentation.dto;

import com.facturacion.application.ports.output.TopProductoAnalitico;

import java.math.BigDecimal;
import java.util.List;

public class DashboardDto {

    public record DashboardResumenResponse(
            long totalClientes,
            long totalProductos,
            long totalProductosActivos,
            long totalFacturas,
            BigDecimal ventasAcumuladas,
            List<FacturaDto.FacturaResponse> facturasRecientes,
            List<TopProductoAnalitico> productosMasVendidos
    ) {
    }
}
