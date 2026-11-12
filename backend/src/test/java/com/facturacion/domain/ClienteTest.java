package com.facturacion.domain;

import com.facturacion.domain.exception.ValidacionDominioException;
import com.facturacion.domain.model.Cliente;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    @Test
    void debeCrearClienteValido() {
        Cliente cliente = Cliente.crearNuevo(
                "Carlos Perez",
                "Bogota",
                "carlos@example.com",
                "3001234567",
                "Regular"
        );

        assertNotNull(cliente.getIdCliente());
        assertEquals("Carlos Perez", cliente.getNombreCompleto());
        assertEquals("carlos@example.com", cliente.getCorreo());
        assertEquals("Bogota", cliente.getCiudad());
        assertNotNull(cliente.getFechaRegistro());
    }

    @Test
    void debeRechazarCorreoInvalido() {
        assertThrows(ValidacionDominioException.class, () -> Cliente.crearNuevo(
                "Carlos Perez",
                "Bogota",
                "correo-invalido",
                "3001234567",
                null
        ));
    }

    @Test
    void debeRechazarNombreVacio() {
        assertThrows(ValidacionDominioException.class, () -> Cliente.crearNuevo(
                "   ",
                "Bogota",
                "carlos@example.com",
                "3001234567",
                null
        ));
    }
}
