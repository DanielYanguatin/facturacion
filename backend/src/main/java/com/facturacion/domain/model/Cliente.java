package com.facturacion.domain.model;

import com.facturacion.domain.exception.ValidacionDominioException;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

public class Cliente {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private UUID idCliente;
    private String nombreCompleto;
    private String ciudad;
    private String correo;
    private String telefono;
    private String categoria;
    private Instant fechaRegistro;
    private Instant actualizadoEn;

    public Cliente() {
    }

    public static Cliente crearNuevo(
            String nombreCompleto,
            String ciudad,
            String correo,
            String telefono,
            String categoria
    ) {
        Instant ahora = Instant.now();
        Cliente cliente = new Cliente();
        cliente.idCliente = UUID.randomUUID();
        cliente.fechaRegistro = ahora;
        cliente.actualizadoEn = ahora;
        cliente.aplicarDatos(nombreCompleto, ciudad, correo, telefono, categoria);
        return cliente;
    }

    public static Cliente reconstruir(
            UUID idCliente,
            String nombreCompleto,
            String ciudad,
            String correo,
            String telefono,
            String categoria,
            Instant fechaRegistro,
            Instant actualizadoEn
    ) {
        Cliente cliente = new Cliente();
        cliente.idCliente = idCliente;
        cliente.nombreCompleto = nombreCompleto;
        cliente.ciudad = ciudad;
        cliente.correo = correo;
        cliente.telefono = telefono;
        cliente.categoria = categoria;
        cliente.fechaRegistro = fechaRegistro;
        cliente.actualizadoEn = actualizadoEn;
        return cliente;
    }

    public void actualizar(
            String nombreCompleto,
            String ciudad,
            String correo,
            String telefono,
            String categoria
    ) {
        aplicarDatos(nombreCompleto, ciudad, correo, telefono, categoria);
        this.actualizadoEn = Instant.now();
    }

    private void aplicarDatos(
            String nombreCompleto,
            String ciudad,
            String correo,
            String telefono,
            String categoria
    ) {
        this.nombreCompleto = exigirTexto(nombreCompleto, "nombreCompleto");
        this.ciudad = exigirTexto(ciudad, "ciudad");
        this.correo = validarCorreo(correo);
        this.telefono = exigirTexto(telefono, "telefono");
        this.categoria = (categoria != null && !categoria.trim().isEmpty()) ? categoria.trim() : null;
    }

    private String exigirTexto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ValidacionDominioException("El campo '" + campo + "' es obligatorio.");
        }
        return valor.trim();
    }

    private String validarCorreo(String correo) {
        String normalizado = correo == null ? "" : correo.trim().toLowerCase();
        if (normalizado.isEmpty() || !EMAIL_PATTERN.matcher(normalizado).matches()) {
            throw new ValidacionDominioException("Correo electronico invalido: '" + correo + "'.");
        }
        return normalizado;
    }

    public UUID getIdCliente() {
        return idCliente;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCategoria() {
        return categoria;
    }

    public Instant getFechaRegistro() {
        return fechaRegistro;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }
}
