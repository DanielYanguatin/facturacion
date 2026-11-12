package com.facturacion.infrastructure.persistence.mysql;

import com.facturacion.application.ports.output.InfraestructuraNoDisponibleException;
import com.facturacion.application.ports.output.OutboxRepositoryPort;
import com.facturacion.domain.model.OutboxEvento;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class MySQLOutboxRepositoryAdapter implements OutboxRepositoryPort {

    private final SpringDataOutboxRepository outboxRepository;

    public MySQLOutboxRepositoryAdapter(SpringDataOutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxEvento> buscarPendientesParaProcesar(int limite) {
        try {
            int max = Math.max(1, Math.min(limite, 50));
            return outboxRepository.findByEstadoInOrderByCreadoEnAsc(
                    Set.of("PENDIENTE", "ERROR"),
                    PageRequest.of(0, max)
            ).stream().map(this::toDomain).toList();
        } catch (DataAccessException exc) {
            throw new InfraestructuraNoDisponibleException("MySQL", "Error al buscar eventos pendientes de outbox en MySQL", exc);
        }
    }

    @Override
    @Transactional
    public void marcarEnviado(Long idEvento) {
        try {
            outboxRepository.findById(idEvento).ifPresent(evento -> {
                evento.setEstado("ENVIADO");
                evento.setUltimoError(null);
                evento.setEnviadoEn(LocalDateTime.now());
                outboxRepository.save(evento);
            });
        } catch (DataAccessException exc) {
            throw new InfraestructuraNoDisponibleException("MySQL", "Error al marcar evento outbox como enviado en MySQL", exc);
        }
    }

    @Override
    @Transactional
    public void marcarError(Long idEvento, String mensajeError) {
        try {
            outboxRepository.findById(idEvento).ifPresent(evento -> {
                evento.setEstado("ERROR");
                evento.setIntentos(evento.getIntentos() == null ? 1 : evento.getIntentos() + 1);
                evento.setUltimoError(mensajeError);
                outboxRepository.save(evento);
            });
        } catch (DataAccessException exc) {
            throw new InfraestructuraNoDisponibleException("MySQL", "Error al marcar evento outbox con error en MySQL", exc);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> contarPorEstado() {
        try {
            Map<String, Long> resultado = new HashMap<>();
            resultado.put("PENDIENTE", 0L);
            resultado.put("ENVIADO", 0L);
            resultado.put("ERROR", 0L);
            List<Object[]> filas = outboxRepository.contarAgrupadoPorEstado();
            for (Object[] fila : filas) {
                if (fila != null && fila.length >= 2 && fila[0] != null && fila[1] != null) {
                    resultado.put(fila[0].toString(), ((Number) fila[1]).longValue());
                }
            }
            return resultado;
        } catch (DataAccessException exc) {
            throw new InfraestructuraNoDisponibleException("MySQL", "Error al contar outbox por estado en MySQL", exc);
        }
    }

    private OutboxEvento toDomain(OutboxAnaliticoJpaEntity entity) {
        return new OutboxEvento(
                entity.getIdEvento(),
                entity.getIdFactura(),
                entity.getPayloadJson(),
                entity.getEstado(),
                entity.getIntentos(),
                entity.getUltimoError(),
                entity.getCreadoEn(),
                entity.getEnviadoEn()
        );
    }
}
