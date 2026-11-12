package com.facturacion.infrastructure.persistence.mysql;

import com.facturacion.application.ports.output.OutboxPayloadCodec;
import com.facturacion.domain.model.EventoAnaliticoFactura;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;

@Component
public class JacksonOutboxPayloadCodec implements OutboxPayloadCodec {

    private final ObjectMapper objectMapper;

    public JacksonOutboxPayloadCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String serializar(List<EventoAnaliticoFactura> eventos) {
        try {
            return objectMapper.writeValueAsString(eventos);
        } catch (JsonProcessingException exc) {
            throw new UncheckedIOException("Error al serializar eventos analiticos a JSON", exc);
        }
    }

    @Override
    public List<EventoAnaliticoFactura> deserializar(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(payloadJson, new TypeReference<>() {});
        } catch (IOException exc) {
            throw new UncheckedIOException("Error al deserializar eventos analiticos desde JSON", exc);
        }
    }
}
