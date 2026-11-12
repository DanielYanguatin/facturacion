package com.facturacion.application.ports.input;

import java.util.List;
import java.util.Map;

public interface AnaliticaOutboxUseCase {

    record ResultadoProcesamientoOutbox(
            int procesados,
            int enviados,
            int conError
    ) {
    }

    ResultadoProcesamientoOutbox procesarPendientes(int limite);

    Map<String, Long> obtenerEstadoOutbox();

    List<Map<String, Object>> consultarRegistrosDruid(int limite);
}
