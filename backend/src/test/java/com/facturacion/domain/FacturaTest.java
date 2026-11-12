package com.facturacion.domain;

import com.facturacion.domain.exception.ReglaNegocioException;
import com.facturacion.domain.exception.ValidacionDominioException;
import com.facturacion.domain.model.DetalleFactura;
import com.facturacion.domain.model.Factura;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FacturaTest {

    @Test
    void debeCalcularTotalCorrectamente() {
        DetalleFactura d1 = DetalleFactura.crear(
                "prod-1",
                "Teclado",
                "Perifericos",
                new BigDecimal("100.00"),
                2
        );
        DetalleFactura d2 = DetalleFactura.crear(
                "prod-2",
                "Raton",
                "Perifericos",
                new BigDecimal("50.50"),
                3
        );

        assertEquals(new BigDecimal("200.00"), d1.getTotalProducto());
        assertEquals(new BigDecimal("151.50"), d2.getTotalProducto());

        Factura factura = Factura.crearNueva(
                "FAC-001",
                "cli-uuid",
                LocalDateTime.now(),
                List.of(d1, d2)
        );

        assertEquals(new BigDecimal("351.50"), factura.getSubtotal());
        assertEquals(new BigDecimal("351.50"), factura.getTotal());
        assertEquals("EFECTIVO", factura.getMetodoPago());
        assertEquals("EMITIDA", factura.getEstado());
    }

    @Test
    void debeRechazarFacturaSinProductos() {
        assertThrows(ReglaNegocioException.class, () -> Factura.crearNueva(
                "FAC-002",
                "cli-uuid",
                LocalDateTime.now(),
                Collections.emptyList()
        ));
    }

    @Test
    void debeRechazarCantidadCeroONegativa() {
        assertThrows(ValidacionDominioException.class, () -> DetalleFactura.crear(
                "prod-1",
                "Teclado",
                "Perifericos",
                new BigDecimal("100.00"),
                0
        ));
    }
}
