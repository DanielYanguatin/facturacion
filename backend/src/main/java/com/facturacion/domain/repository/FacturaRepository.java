package com.facturacion.domain.repository;

import com.facturacion.domain.model.EventoAnaliticoFactura;
import com.facturacion.domain.model.Factura;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface FacturaRepository {
    Factura guardarTransaccionalConOutbox(
            Factura factura,
            Function<Factura, List<EventoAnaliticoFactura>> constructorEventos
    );

    Optional<Factura> buscarPorId(Long idFactura);

    Optional<Factura> buscarPorNumero(String numeroFactura);

    List<Factura> listar(
            String numeroFactura,
            String idCliente,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta
    );

    long contarTotal();

    BigDecimal sumarVentasAcumuladas();
}
