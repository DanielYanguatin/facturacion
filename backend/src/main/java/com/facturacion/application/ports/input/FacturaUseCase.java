package com.facturacion.application.ports.input;

import com.facturacion.domain.model.Cliente;
import com.facturacion.domain.model.Factura;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FacturaUseCase {

    record ItemFacturaCommand(
            String idProducto,
            Integer cantidad
    ) {
    }

    record GenerarFacturaCommand(
            String idCliente,
            List<ItemFacturaCommand> items
    ) {
    }

    record FacturaEnriquecida(
            Factura factura,
            Cliente cliente,
            Map<String, String> imagenesPorProducto
    ) {
    }

    FacturaEnriquecida generarFactura(GenerarFacturaCommand command);

    FacturaEnriquecida obtenerPorId(Long idFactura);

    FacturaEnriquecida obtenerPorNumero(String numeroFactura);

    List<FacturaEnriquecida> listarFacturas(
            String numeroFactura,
            String idCliente,
            LocalDate fechaDesde,
            LocalDate fechaHasta
    );
}
