package com.facturacion.infrastructure.persistence.cassandra;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("clientes_por_id")
public class ClientePorIdEntity {

    @PrimaryKey("id_cliente")
    private UUID idCliente;

    @Column("nombre_completo")
    private String nombreCompleto;

    @Column("ciudad")
    private String ciudad;

    @Column("correo")
    private String correo;

    @Column("telefono")
    private String telefono;

    @Column("categoria")
    private String categoria;

    @Column("fecha_registro")
    private Instant fechaRegistro;

    @Column("actualizado_en")
    private Instant actualizadoEn;

    public ClientePorIdEntity() {
    }

    public ClientePorIdEntity(
            UUID idCliente,
            String nombreCompleto,
            String ciudad,
            String correo,
            String telefono,
            String categoria,
            Instant fechaRegistro,
            Instant actualizadoEn
    ) {
        this.idCliente = idCliente;
        this.nombreCompleto = nombreCompleto;
        this.ciudad = ciudad;
        this.correo = correo;
        this.telefono = telefono;
        this.categoria = categoria;
        this.fechaRegistro = fechaRegistro;
        this.actualizadoEn = actualizadoEn;
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

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Instant getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Instant fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(Instant actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }
}
