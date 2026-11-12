package com.facturacion.application.ports.input;

import com.facturacion.application.ports.output.TopProductoAnalitico;

import java.math.BigDecimal;
import java.util.List;

public interface DashboardUseCase {

    record DashboardResumen(
            long totalClientes,
            long totalProductos,
            long totalProductosActivos,
            long totalFacturas,
            BigDecimal ventasAcumuladas,
            List<FacturaUseCase.FacturaEnriquecida> facturasRecientes,
            List<TopProductoAnalitico> productosMasVendidos
    ) {
    }

    DashboardResumen obtenerResumen();
}
