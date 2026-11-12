package com.facturacion.application.ports.output;

import com.facturacion.domain.model.EventoAnaliticoFactura;

import java.util.List;

public interface OutboxPayloadCodec {
    String serializar(List<EventoAnaliticoFactura> eventos);

    List<EventoAnaliticoFactura> deserializar(String payloadJson);
}
