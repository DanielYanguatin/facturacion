package com.facturacion.application.usecase;

import com.facturacion.application.ports.input.AnaliticaOutboxUseCase;
import com.facturacion.application.ports.output.DruidAnalyticsPort;
import com.facturacion.application.ports.output.OutboxPayloadCodec;
import com.facturacion.application.ports.output.OutboxRepositoryPort;
import com.facturacion.domain.model.EventoAnaliticoFactura;
import com.facturacion.domain.model.OutboxEvento;

import java.util.List;
import java.util.Map;

public class AnaliticaOutboxUseCaseImpl implements AnaliticaOutboxUseCase {

    private final OutboxRepositoryPort outboxRepositoryPort;
    private final DruidAnalyticsPort druidAnalyticsPort;
    private final OutboxPayloadCodec outboxPayloadCodec;

    public AnaliticaOutboxUseCaseImpl(
            OutboxRepositoryPort outboxRepositoryPort,
            DruidAnalyticsPort druidAnalyticsPort,
            OutboxPayloadCodec outboxPayloadCodec
    ) {
        this.outboxRepositoryPort = outboxRepositoryPort;
        this.druidAnalyticsPort = druidAnalyticsPort;
        this.outboxPayloadCodec = outboxPayloadCodec;
    }

    @Override
    public ResultadoProcesamientoOutbox procesarPendientes(int limite) {
        List<OutboxEvento> pendientes = outboxRepositoryPort.buscarPendientesParaProcesar(limite);
        int enviados = 0;
        int conError = 0;

        for (OutboxEvento evento : pendientes) {
            try {
                List<EventoAnaliticoFactura> registros = outboxPayloadCodec.deserializar(evento.payloadJson());
                if (!registros.isEmpty()) {
                    druidAnalyticsPort.ingestarEventos(registros);
                }
                outboxRepositoryPort.marcarEnviado(evento.idEvento());
                enviados++;
            } catch (RuntimeException exc) {
                String mensaje = exc.getMessage() == null ? exc.getClass().getSimpleName() : exc.getMessage();
                outboxRepositoryPort.marcarError(evento.idEvento(), mensaje);
                conError++;
            }
        }
        return new ResultadoProcesamientoOutbox(pendientes.size(), enviados, conError);
    }

    @Override
    public Map<String, Long> obtenerEstadoOutbox() {
        return outboxRepositoryPort.contarPorEstado();
    }

    @Override
    public List<Map<String, Object>> consultarRegistrosDruid(int limite) {
        return druidAnalyticsPort.consultarRegistrosRecientes(limite);
    }
}
