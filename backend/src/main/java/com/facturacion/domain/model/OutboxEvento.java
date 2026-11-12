package com.facturacion.domain.model;

import java.time.LocalDateTime;

public record OutboxEvento(
        Long idEvento,
        Long idFactura,
        String payloadJson,
        String estado,
        Integer intentos,
        String ultimoError,
        LocalDateTime creadoEn,
        LocalDateTime enviadoEn
) {
}
