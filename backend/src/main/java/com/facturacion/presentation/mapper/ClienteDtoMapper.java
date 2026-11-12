package com.facturacion.presentation.mapper;

import com.facturacion.domain.model.Cliente;
import com.facturacion.presentation.dto.ClienteDto.ClienteResponse;

public final class ClienteDtoMapper {

    private ClienteDtoMapper() {
    }

    public static ClienteResponse toResponse(Cliente domain) {
        if (domain == null) {
            return null;
        }
        return new ClienteResponse(
                domain.getIdCliente(),
                domain.getNombreCompleto(),
                domain.getCiudad(),
                domain.getCorreo(),
                domain.getTelefono(),
                domain.getCategoria(),
                domain.getFechaRegistro(),
                domain.getActualizadoEn()
        );
    }
}
