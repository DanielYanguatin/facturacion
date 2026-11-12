package com.facturacion.presentation.controller;

import com.facturacion.application.ports.input.AnaliticaOutboxUseCase;
import com.facturacion.application.ports.input.AnaliticaOutboxUseCase.ResultadoProcesamientoOutbox;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analitica")
public class AnaliticaController {

    private final AnaliticaOutboxUseCase analiticaOutboxUseCase;

    public AnaliticaController(AnaliticaOutboxUseCase analiticaOutboxUseCase) {
        this.analiticaOutboxUseCase = analiticaOutboxUseCase;
    }

    @GetMapping("/outbox/estado")
    public ResponseEntity<Map<String, Long>> obtenerEstadoOutbox() {
        return ResponseEntity.ok(analiticaOutboxUseCase.obtenerEstadoOutbox());
    }

    @PostMapping("/outbox/procesar")
    public ResponseEntity<ResultadoProcesamientoOutbox> procesarOutbox(
            @RequestParam(name = "limite", required = false, defaultValue = "10") int limite
    ) {
        return ResponseEntity.ok(analiticaOutboxUseCase.procesarPendientes(limite));
    }

    @GetMapping("/druid/registros")
    public ResponseEntity<List<Map<String, Object>>> consultarRegistrosDruid(
            @RequestParam(name = "limite", required = false, defaultValue = "20") int limite
    ) {
        return ResponseEntity.ok(analiticaOutboxUseCase.consultarRegistrosDruid(limite));
    }
}
