package com.facturacion.application.ports.output;

import com.facturacion.domain.model.EventoAnaliticoFactura;

import java.util.List;
import java.util.Map;

public interface DruidAnalyticsPort {
    void ingestarEventos(List<EventoAnaliticoFactura> eventos);

    List<TopProductoAnalitico> consultarProductosMasVendidos(int limite);

    List<Map<String, Object>> consultarRegistrosRecientes(int limite);

    boolean estaDisponible();
}
