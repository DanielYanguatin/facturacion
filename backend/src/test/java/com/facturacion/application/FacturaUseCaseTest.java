package com.facturacion.application;

import com.facturacion.application.ports.input.FacturaUseCase;
import com.facturacion.application.ports.input.FacturaUseCase.FacturaEnriquecida;
import com.facturacion.application.ports.input.FacturaUseCase.GenerarFacturaCommand;
import com.facturacion.application.ports.input.FacturaUseCase.ItemFacturaCommand;
import com.facturacion.application.usecase.FacturaUseCaseImpl;
import com.facturacion.domain.exception.ReglaNegocioException;
import com.facturacion.domain.model.Cliente;
import com.facturacion.domain.model.Factura;
import com.facturacion.domain.model.Producto;
import com.facturacion.domain.repository.ClienteRepository;
import com.facturacion.domain.repository.FacturaRepository;
import com.facturacion.domain.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacturaUseCaseTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private FacturaRepository facturaRepository;

    private FacturaUseCaseImpl facturaUseCase;

    @BeforeEach
    void setUp() {
        facturaUseCase = new FacturaUseCaseImpl(clienteRepository, productoRepository, facturaRepository);
    }

    @Test
    void debeGenerarFacturaConPreciosRealesDeMongoDB() {
        UUID clienteId = UUID.randomUUID();
        Cliente cliente = Cliente.crearNuevo("Ana Gomez", "Medellin", "ana@example.com", "3110000000", "VIP");
        cliente = Cliente.reconstruir(clienteId, cliente.getNombreCompleto(), cliente.getCiudad(),
                cliente.getCorreo(), cliente.getTelefono(), cliente.getCategoria(), cliente.getFechaRegistro(), cliente.getActualizadoEn());

        Producto producto = Producto.crearNuevo("Auriculares Pro", "Inalambricos", "Audio", new BigDecimal("150000.00"), 20);
        producto.setIdProducto("prod-123");

        when(clienteRepository.buscarPorId(clienteId)).thenReturn(Optional.of(cliente));
        when(productoRepository.buscarPorId("prod-123")).thenReturn(Optional.of(producto));
        when(facturaRepository.guardarTransaccionalConOutbox(any(Factura.class), any())).thenAnswer(invocation -> invocation.getArgument(0));

        GenerarFacturaCommand command = new GenerarFacturaCommand(
                clienteId.toString(),
                List.of(new ItemFacturaCommand("prod-123", 2))
        );

        FacturaEnriquecida resultado = facturaUseCase.generarFactura(command);

        assertNotNull(resultado);
        assertEquals(new BigDecimal("300000.00"), resultado.factura().getTotal());
        assertEquals(1, resultado.factura().getDetalles().size());
        assertEquals("Auriculares Pro", resultado.factura().getDetalles().getFirst().getNombreProductoSnapshot());
        verify(facturaRepository, times(1)).guardarTransaccionalConOutbox(any(), any());
    }

    @Test
    void debeRechazarFacturacionDeProductoInactivo() {
        UUID clienteId = UUID.randomUUID();
        Cliente cliente = Cliente.crearNuevo("Pedro", "Cali", "pedro@example.com", "3120000000", null);

        Producto inactivo = Producto.crearNuevo("Teclado Viejo", "USB", "Perifericos", new BigDecimal("30000.00"), 5);
        inactivo.setIdProducto("prod-999");
        inactivo.desactivar();

        when(clienteRepository.buscarPorId(clienteId)).thenReturn(Optional.of(cliente));
        when(productoRepository.buscarPorId("prod-999")).thenReturn(Optional.of(inactivo));

        GenerarFacturaCommand command = new GenerarFacturaCommand(
                clienteId.toString(),
                List.of(new ItemFacturaCommand("prod-999", 1))
        );

        assertThrows(ReglaNegocioException.class, () -> facturaUseCase.generarFactura(command));
        verify(facturaRepository, never()).guardarTransaccionalConOutbox(any(), any());
    }
}
