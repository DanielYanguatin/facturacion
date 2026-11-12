package com.facturacion.infrastructure.druid;

import com.facturacion.application.ports.input.AnaliticaOutboxUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxScheduledWorker {

    private static final Logger log = LoggerFactory.getLogger(OutboxScheduledWorker.class);

    private final AnaliticaOutboxUseCase analiticaOutboxUseCase;

    public OutboxScheduledWorker(AnaliticaOutboxUseCase analiticaOutboxUseCase) {
        this.analiticaOutboxUseCase = analiticaOutboxUseCase;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-seconds:5}000")
    public void procesarEventosOutbox() {
        try {
            var resultado = analiticaOutboxUseCase.procesarPendientes(10);
            if (resultado.procesados() > 0) {
                log.info("Outbox procesado: {} eventos (enviados: {}, errores: {})",
                        resultado.procesados(), resultado.enviados(), resultado.conError());
            }
        } catch (Exception exc) {
            log.warn("Fallo transitorio en worker de Outbox a Druid: {}", exc.getMessage());
        }
    }
}
