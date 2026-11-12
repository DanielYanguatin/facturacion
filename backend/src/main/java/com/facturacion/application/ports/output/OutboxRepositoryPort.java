package com.facturacion.application.ports.output;

import com.facturacion.domain.model.OutboxEvento;

import java.util.List;
import java.util.Map;

public interface OutboxRepositoryPort {
    List<OutboxEvento> buscarPendientesParaProcesar(int limite);

    void marcarEnviado(Long idEvento);

    void marcarError(Long idEvento, String mensajeError);

    Map<String, Long> contarPorEstado();
}
