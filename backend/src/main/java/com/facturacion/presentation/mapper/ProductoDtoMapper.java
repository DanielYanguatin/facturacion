package com.facturacion.presentation.mapper;

import com.facturacion.domain.model.Producto;
import com.facturacion.presentation.dto.ProductoDto.ProductoResponse;

public final class ProductoDtoMapper {

    private ProductoDtoMapper() {
    }

    public static ProductoResponse toResponse(Producto domain) {
        if (domain == null) {
            return null;
        }
        return new ProductoResponse(
                domain.getIdProducto(),
                domain.getNombre(),
                domain.getDescripcion(),
                domain.getCategoria(),
                domain.getPrecioUnitario(),
                domain.getCantidadDisponible(),
                domain.getImagenId(),
                domain.getEstado(),
                domain.getFechaRegistro(),
                domain.getActualizadoEn()
        );
    }
}
