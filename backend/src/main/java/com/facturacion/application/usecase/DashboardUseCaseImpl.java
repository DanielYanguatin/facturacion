package com.facturacion.application.usecase;

import com.facturacion.application.ports.input.DashboardUseCase;
import com.facturacion.application.ports.input.FacturaUseCase;
import com.facturacion.application.ports.output.DruidAnalyticsPort;
import com.facturacion.application.ports.output.TopProductoAnalitico;
import com.facturacion.domain.model.Factura;
import com.facturacion.domain.repository.ClienteRepository;
import com.facturacion.domain.repository.FacturaRepository;
import com.facturacion.domain.repository.ProductoRepository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class DashboardUseCaseImpl implements DashboardUseCase {

    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final FacturaRepository facturaRepository;
    private final FacturaUseCaseImpl facturaUseCase;
    private final DruidAnalyticsPort druidAnalyticsPort;

    public DashboardUseCaseImpl(
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository,
            FacturaRepository facturaRepository,
            FacturaUseCaseImpl facturaUseCase,
            DruidAnalyticsPort druidAnalyticsPort
    ) {
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.facturaRepository = facturaRepository;
        this.facturaUseCase = facturaUseCase;
        this.druidAnalyticsPort = druidAnalyticsPort;
    }

    @Override
    public DashboardResumen obtenerResumen() {
        long totalClientes = clienteRepository.contarTotal();
        long totalProductos = productoRepository.contarTotal();
        long totalProductosActivos = productoRepository.contarActivos();
        long totalFacturas = facturaRepository.contarTotal();
        BigDecimal ventasAcumuladas = facturaRepository.sumarVentasAcumuladas();

        List<Factura> recientes = facturaRepository.listar(null, null, null, null)
                .stream()
                .limit(5)
                .toList();
        List<FacturaUseCase.FacturaEnriquecida> facturasRecientes = facturaUseCase.enriquecerFacturas(recientes);

        List<TopProductoAnalitico> topProductos = consultarTopProductosSeguro();

        return new DashboardResumen(
                totalClientes,
                totalProductos,
                totalProductosActivos,
                totalFacturas,
                ventasAcumuladas,
                facturasRecientes,
                topProductos
        );
    }

    private List<TopProductoAnalitico> consultarTopProductosSeguro() {
        try {
            return druidAnalyticsPort.consultarProductosMasVendidos(5);
        } catch (RuntimeException ignored) {
            return Collections.emptyList();
        }
    }
}
