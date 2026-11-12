package com.facturacion.presentation.controller;

import com.facturacion.application.ports.input.DashboardUseCase;
import com.facturacion.application.ports.input.DashboardUseCase.DashboardResumen;
import com.facturacion.presentation.dto.DashboardDto.DashboardResumenResponse;
import com.facturacion.presentation.dto.FacturaDto.FacturaResponse;
import com.facturacion.presentation.mapper.FacturaDtoMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardUseCase dashboardUseCase;

    public DashboardController(DashboardUseCase dashboardUseCase) {
        this.dashboardUseCase = dashboardUseCase;
    }

    @GetMapping("/resumen")
    public ResponseEntity<DashboardResumenResponse> obtenerResumen() {
        DashboardResumen resumen = dashboardUseCase.obtenerResumen();

        List<FacturaResponse> facturasDto = resumen.facturasRecientes()
                .stream()
                .map(FacturaDtoMapper::toResponse)
                .toList();

        DashboardResumenResponse response = new DashboardResumenResponse(
                resumen.totalClientes(),
                resumen.totalProductos(),
                resumen.totalProductosActivos(),
                resumen.totalFacturas(),
                resumen.ventasAcumuladas(),
                facturasDto,
                resumen.productosMasVendidos()
        );
        return ResponseEntity.ok(response);
    }
}
