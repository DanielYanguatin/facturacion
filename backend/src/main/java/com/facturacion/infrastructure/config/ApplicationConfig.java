package com.facturacion.infrastructure.config;

import com.facturacion.application.ports.input.AnaliticaOutboxUseCase;
import com.facturacion.application.ports.input.ClienteUseCase;
import com.facturacion.application.ports.input.DashboardUseCase;
import com.facturacion.application.ports.input.FacturaUseCase;
import com.facturacion.application.ports.input.ProductoUseCase;
import com.facturacion.application.ports.output.DruidAnalyticsPort;
import com.facturacion.application.ports.output.ImagenStoragePort;
import com.facturacion.application.ports.output.OutboxPayloadCodec;
import com.facturacion.application.ports.output.OutboxRepositoryPort;
import com.facturacion.application.usecase.AnaliticaOutboxUseCaseImpl;
import com.facturacion.application.usecase.ClienteUseCaseImpl;
import com.facturacion.application.usecase.DashboardUseCaseImpl;
import com.facturacion.application.usecase.FacturaUseCaseImpl;
import com.facturacion.application.usecase.ProductoUseCaseImpl;
import com.facturacion.domain.repository.ClienteRepository;
import com.facturacion.domain.repository.FacturaRepository;
import com.facturacion.domain.repository.ProductoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public ClienteUseCase clienteUseCase(ClienteRepository clienteRepository) {
        return new ClienteUseCaseImpl(clienteRepository);
    }

    @Bean
    public ProductoUseCase productoUseCase(
            ProductoRepository productoRepository,
            ImagenStoragePort imagenStoragePort
    ) {
        return new ProductoUseCaseImpl(productoRepository, imagenStoragePort);
    }

    @Bean
    public FacturaUseCaseImpl facturaUseCaseImpl(
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository,
            FacturaRepository facturaRepository
    ) {
        return new FacturaUseCaseImpl(clienteRepository, productoRepository, facturaRepository);
    }

    @Bean
    public FacturaUseCase facturaUseCase(FacturaUseCaseImpl facturaUseCaseImpl) {
        return facturaUseCaseImpl;
    }

    @Bean
    public DashboardUseCase dashboardUseCase(
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository,
            FacturaRepository facturaRepository,
            FacturaUseCaseImpl facturaUseCaseImpl,
            DruidAnalyticsPort druidAnalyticsPort
    ) {
        return new DashboardUseCaseImpl(
                clienteRepository,
                productoRepository,
                facturaRepository,
                facturaUseCaseImpl,
                druidAnalyticsPort
        );
    }

    @Bean
    public AnaliticaOutboxUseCase analiticaOutboxUseCase(
            OutboxRepositoryPort outboxRepositoryPort,
            DruidAnalyticsPort druidAnalyticsPort,
            OutboxPayloadCodec outboxPayloadCodec
    ) {
        return new AnaliticaOutboxUseCaseImpl(outboxRepositoryPort, druidAnalyticsPort, outboxPayloadCodec);
    }
}
