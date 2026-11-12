package com.facturacion.presentation.mapper;

import com.facturacion.application.ports.input.FacturaUseCase.FacturaEnriquecida;
import com.facturacion.domain.model.Cliente;
import com.facturacion.domain.model.DetalleFactura;
import com.facturacion.domain.model.Factura;
import com.facturacion.presentation.dto.FacturaDto.DetalleFacturaResponse;
import com.facturacion.presentation.dto.FacturaDto.FacturaResponse;

import java.util.ArrayList;
import java.util.List;

public final class FacturaDtoMapper {

    private FacturaDtoMapper() {
    }

    public static FacturaResponse toResponse(FacturaEnriquecida enriquecida) {
        if (enriquecida == null) {
            return null;
        }
        Factura f = enriquecida.factura();
        Cliente c = enriquecida.cliente();

        List<DetalleFacturaResponse> detallesDto = new ArrayList<>();
        if (f.getDetalles() != null) {
            for (DetalleFactura d : f.getDetalles()) {
                String imagenId = enriquecida.imagenesPorProducto() != null
                        ? enriquecida.imagenesPorProducto().get(d.getIdProducto())
                        : null;

                detallesDto.add(new DetalleFacturaResponse(
                        d.getIdDetalle(),
                        d.getIdProducto(),
                        d.getNombreProductoSnapshot(),
                        d.getCategoriaProductoSnapshot(),
                        d.getPrecioUnitario(),
                        d.getCantidad(),
                        d.getTotalProducto(),
                        imagenId
                ));
            }
        }

        return new FacturaResponse(
                f.getIdFactura(),
                f.getNumeroFactura(),
                f.getIdCliente(),
                c != null ? c.getNombreCompleto() : "Cliente no encontrado",
                c != null ? c.getCorreo() : "",
                c != null ? c.getCiudad() : "",
                c != null ? c.getTelefono() : "",
                c != null ? c.getCategoria() : null,
                f.getFechaFactura(),
                f.getSubtotal(),
                f.getTotal(),
                f.getMetodoPago(),
                f.getEstado(),
                detallesDto
        );
    }
}
