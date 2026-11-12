package com.facturacion.domain;

import com.facturacion.domain.exception.ValidacionDominioException;
import com.facturacion.domain.model.Producto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductoTest {

    @Test
    void debeCrearProductoValido() {
        Producto producto = Producto.crearNuevo(
                "Monitor 27",
                "Monitor IPS 144Hz",
                "Monitores",
                new BigDecimal("850000.00"),
                10
        );

        assertEquals("Monitor 27", producto.getNombre());
        assertEquals("activo", producto.getEstado());
        assertTrue(producto.estaActivo());
        assertEquals(new BigDecimal("850000.00"), producto.getPrecioUnitario());
        assertEquals(10, producto.getCantidadDisponible());
    }

    @Test
    void debeRechazarPrecioNegativo() {
        assertThrows(ValidacionDominioException.class, () -> Producto.crearNuevo(
                "Teclado",
                "Mecanico",
                "Perifericos",
                new BigDecimal("-500.00"),
                5
        ));
    }

    @Test
    void debePermitirDesactivarProducto() {
        Producto producto = Producto.crearNuevo(
                "Raton",
                "Optico",
                "Perifericos",
                new BigDecimal("45000.00"),
                15
        );
        assertTrue(producto.estaActivo());
        producto.desactivar();
        assertFalse(producto.estaActivo());
        assertEquals("inactivo", producto.getEstado());
    }
}
