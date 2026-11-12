package com.facturacion.infrastructure.persistence.mysql;

import com.facturacion.application.ports.output.InfraestructuraNoDisponibleException;
import com.facturacion.application.ports.output.OutboxPayloadCodec;
import com.facturacion.domain.model.DetalleFactura;
import com.facturacion.domain.model.EventoAnaliticoFactura;
import com.facturacion.domain.model.Factura;
import com.facturacion.domain.repository.FacturaRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Component
public class MySQLFacturaRepositoryAdapter implements FacturaRepository {

    private final SpringDataFacturaRepository facturaRepository;
    private final SpringDataOutboxRepository outboxRepository;
    private final OutboxPayloadCodec outboxPayloadCodec;

    public MySQLFacturaRepositoryAdapter(
            SpringDataFacturaRepository facturaRepository,
            SpringDataOutboxRepository outboxRepository,
            OutboxPayloadCodec outboxPayloadCodec
    ) {
        this.facturaRepository = facturaRepository;
        this.outboxRepository = outboxRepository;
        this.outboxPayloadCodec = outboxPayloadCodec;
    }

    @Override
    @Transactional
    public Factura guardarTransaccionalConOutbox(
            Factura factura,
            Function<Factura, List<EventoAnaliticoFactura>> constructorEventos
    ) {
        try {
            FacturaJpaEntity entity = new FacturaJpaEntity(
                    factura.getIdFactura(),
                    factura.getNumeroFactura(),
                    factura.getIdCliente(),
                    factura.getFechaFactura(),
                    factura.getSubtotal(),
                    factura.getTotal(),
                    factura.getMetodoPago(),
                    factura.getEstado()
            );

            for (DetalleFactura detalle : factura.getDetalles()) {
                DetalleFacturaJpaEntity detalleEntity = new DetalleFacturaJpaEntity(
                        detalle.getIdDetalle(),
                        entity,
                        detalle.getIdProducto(),
                        detalle.getNombreProductoSnapshot(),
                        detalle.getCategoriaProductoSnapshot(),
                        detalle.getPrecioUnitario(),
                        detalle.getCantidad(),
                        detalle.getTotalProducto()
                );
                entity.agregarDetalle(detalleEntity);
            }

            FacturaJpaEntity guardada = facturaRepository.save(entity);
            Factura facturaPersistida = toDomain(guardada);

            List<EventoAnaliticoFactura> eventos = constructorEventos.apply(facturaPersistida);
            String payloadJson = outboxPayloadCodec.serializar(eventos);

            OutboxAnaliticoJpaEntity outbox = new OutboxAnaliticoJpaEntity(
                    guardada.getIdFactura(),
                    payloadJson,
                    "PENDIENTE",
                    LocalDateTime.now()
            );
            outboxRepository.save(outbox);

            return facturaPersistida;
        } catch (DataAccessException exc) {
            throw new InfraestructuraNoDisponibleException("MySQL", "Error en transaccion de factura y outbox en MySQL", exc);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Factura> buscarPorId(Long idFactura) {
        try {
            return facturaRepository.findById(idFactura).map(this::toDomain);
        } catch (DataAccessException exc) {
            throw new InfraestructuraNoDisponibleException("MySQL", "Error al buscar factura por ID en MySQL", exc);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Factura> buscarPorNumero(String numeroFactura) {
        try {
            return facturaRepository.findByNumeroFactura(numeroFactura).map(this::toDomain);
        } catch (DataAccessException exc) {
            throw new InfraestructuraNoDisponibleException("MySQL", "Error al buscar factura por numero en MySQL", exc);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Factura> listar(
            String numeroFactura,
            String idCliente,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta
    ) {
        try {
            String numFiltro = (numeroFactura != null && !numeroFactura.isBlank()) ? numeroFactura.trim() : null;
            String cliFiltro = (idCliente != null && !idCliente.isBlank()) ? idCliente.trim() : null;
            return facturaRepository.buscarConFiltros(numFiltro, cliFiltro, fechaDesde, fechaHasta)
                    .stream()
                    .map(this::toDomain)
                    .toList();
        } catch (DataAccessException exc) {
            throw new InfraestructuraNoDisponibleException("MySQL", "Error al listar facturas en MySQL", exc);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long contarTotal() {
        try {
            return facturaRepository.count();
        } catch (DataAccessException exc) {
            throw new InfraestructuraNoDisponibleException("MySQL", "Error al contar facturas en MySQL", exc);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumarVentasAcumuladas() {
        try {
            BigDecimal suma = facturaRepository.sumarTotalVentas();
            return suma != null ? suma : BigDecimal.ZERO;
        } catch (DataAccessException exc) {
            throw new InfraestructuraNoDisponibleException("MySQL", "Error al sumar total de ventas en MySQL", exc);
        }
    }

    private Factura toDomain(FacturaJpaEntity entity) {
        List<DetalleFactura> detalles = new ArrayList<>();
        if (entity.getDetalles() != null) {
            for (DetalleFacturaJpaEntity d : entity.getDetalles()) {
                detalles.add(DetalleFactura.reconstruir(
                        d.getIdDetalle(),
                        entity.getIdFactura(),
                        d.getIdProducto(),
                        d.getNombreProductoSnapshot(),
                        d.getCategoriaProductoSnapshot(),
                        d.getPrecioUnitario(),
                        d.getCantidad(),
                        d.getTotalProducto()
                ));
            }
        }
        return Factura.reconstruir(
                entity.getIdFactura(),
                entity.getNumeroFactura(),
                entity.getIdCliente(),
                entity.getFechaFactura(),
                entity.getSubtotal(),
                entity.getTotal(),
                entity.getMetodoPago(),
                entity.getEstado(),
                detalles
        );
    }
}
