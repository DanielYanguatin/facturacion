package com.facturacion.infrastructure.persistence.cassandra;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table("clientes_por_correo")
public class ClientePorCorreoEntity {

    @PrimaryKey("correo")
    private String correo;

    @Column("id_cliente")
    private UUID idCliente;

    @Column("nombre_completo")
    private String nombreCompleto;

    public ClientePorCorreoEntity() {
    }

    public ClientePorCorreoEntity(String correo, UUID idCliente, String nombreCompleto) {
        this.correo = correo;
        this.idCliente = idCliente;
        this.nombreCompleto = nombreCompleto;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public UUID getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(UUID idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }
}
