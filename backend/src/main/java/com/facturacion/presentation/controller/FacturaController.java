package com.facturacion.presentation.controller;

import com.facturacion.application.ports.input.FacturaUseCase;
import com.facturacion.application.ports.input.FacturaUseCase.FacturaEnriquecida;
import com.facturacion.application.ports.input.FacturaUseCase.GenerarFacturaCommand;
import com.facturacion.application.ports.input.FacturaUseCase.ItemFacturaCommand;
import com.facturacion.presentation.dto.FacturaDto.CrearFacturaRequest;
import com.facturacion.presentation.dto.FacturaDto.FacturaResponse;
import com.facturacion.presentation.mapper.FacturaDtoMapper;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final FacturaUseCase facturaUseCase;

    public FacturaController(FacturaUseCase facturaUseCase) {
        this.facturaUseCase = facturaUseCase;
    }

    @GetMapping
    public ResponseEntity<List<FacturaResponse>> listarFacturas(
            @RequestParam(name = "numeroFactura", required = false) String numeroFactura,
            @RequestParam(name = "idCliente", required = false) String idCliente,
            @RequestParam(name = "fechaDesde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta
    ) {
        List<FacturaResponse> lista = facturaUseCase.listarFacturas(numeroFactura, idCliente, fechaDesde, fechaHasta)
                .stream()
                .map(FacturaDtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaResponse> obtenerPorId(@PathVariable("id") Long idFactura) {
        FacturaEnriquecida enriquecida = facturaUseCase.obtenerPorId(idFactura);
        return ResponseEntity.ok(FacturaDtoMapper.toResponse(enriquecida));
    }

    @GetMapping("/numero/{numero}")
    public ResponseEntity<FacturaResponse> obtenerPorNumero(@PathVariable("numero") String numeroFactura) {
        FacturaEnriquecida enriquecida = facturaUseCase.obtenerPorNumero(numeroFactura);
        return ResponseEntity.ok(FacturaDtoMapper.toResponse(enriquecida));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<FacturaResponse>> obtenerPorCliente(@PathVariable("clienteId") String clienteId) {
        List<FacturaResponse> lista = facturaUseCase.listarFacturas(null, clienteId, null, null)
                .stream()
                .map(FacturaDtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<FacturaResponse> generarFactura(
            @Valid @RequestBody CrearFacturaRequest request
    ) {
        List<ItemFacturaCommand> items = request.items().stream()
                .map(item -> new ItemFacturaCommand(item.idProducto(), item.cantidad()))
                .toList();

        GenerarFacturaCommand cmd = new GenerarFacturaCommand(request.idCliente(), items);
        FacturaEnriquecida generada = facturaUseCase.generarFactura(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(FacturaDtoMapper.toResponse(generada));
    }
}
