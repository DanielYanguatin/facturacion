package com.facturacion.domain.model;

import com.facturacion.domain.exception.ValidacionDominioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

public class Producto {

    public static final String ESTADO_ACTIVO = "activo";
    public static final String ESTADO_INACTIVO = "inactivo";

    private String idProducto;
    private String nombre;
    private String descripcion;
    private String categoria;
    private BigDecimal precioUnitario;
    private Integer cantidadDisponible;
    private String imagenId;
    private String estado;
    private Instant fechaRegistro;
    private Instant actualizadoEn;

    public Producto() {
    }

    public static Producto crearNuevo(
            String nombre,
            String descripcion,
            String categoria,
            BigDecimal precioUnitario,
            Integer cantidadDisponible
    ) {
        Instant ahora = Instant.now();
        Producto producto = new Producto();
        producto.estado = ESTADO_ACTIVO;
        producto.fechaRegistro = ahora;
        producto.actualizadoEn = ahora;
        producto.aplicarDatos(nombre, descripcion, categoria, precioUnitario, cantidadDisponible);
        return producto;
    }

    public static Producto reconstruir(
            String idProducto,
            String nombre,
            String descripcion,
            String categoria,
            BigDecimal precioUnitario,
            Integer cantidadDisponible,
            String imagenId,
            String estado,
            Instant fechaRegistro,
            Instant actualizadoEn
    ) {
        Producto producto = new Producto();
        producto.idProducto = idProducto;
        producto.nombre = nombre;
        producto.descripcion = descripcion;
        producto.categoria = categoria;
        producto.precioUnitario = precioUnitario;
        producto.cantidadDisponible = cantidadDisponible;
        producto.imagenId = imagenId;
        producto.estado = estado;
        producto.fechaRegistro = fechaRegistro;
        producto.actualizadoEn = actualizadoEn;
        return producto;
    }

    public void actualizar(
            String nombre,
            String descripcion,
            String categoria,
            BigDecimal precioUnitario,
            Integer cantidadDisponible,
            String estado
    ) {
        aplicarDatos(nombre, descripcion, categoria, precioUnitario, cantidadDisponible);
        if (estado != null && !estado.trim().isEmpty()) {
            String normalizado = estado.trim().toLowerCase();
            if (!ESTADO_ACTIVO.equals(normalizado) && !ESTADO_INACTIVO.equals(normalizado)) {
                throw new ValidacionDominioException("Estado de producto invalido: '" + estado + "'.");
            }
            this.estado = normalizado;
        }
        this.actualizadoEn = Instant.now();
    }

    public void desactivar() {
        this.estado = ESTADO_INACTIVO;
        this.actualizadoEn = Instant.now();
    }

    public void asignarImagen(String imagenId) {
        this.imagenId = imagenId;
        this.actualizadoEn = Instant.now();
    }

    public boolean estaActivo() {
        return ESTADO_ACTIVO.equalsIgnoreCase(this.estado);
    }

    private void aplicarDatos(
            String nombre,
            String descripcion,
            String categoria,
            BigDecimal precioUnitario,
            Integer cantidadDisponible
    ) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidacionDominioException("El nombre del producto es obligatorio.");
        }
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new ValidacionDominioException("La categoria del producto es obligatoria.");
        }
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionDominioException("El precio unitario no puede ser negativo.");
        }
        int stock = cantidadDisponible == null ? 0 : cantidadDisponible;
        if (stock < 0) {
            throw new ValidacionDominioException("La cantidad disponible no puede ser negativa.");
        }
        this.nombre = nombre.trim();
        this.descripcion = descripcion == null ? "" : descripcion.trim();
        this.categoria = categoria.trim();
        this.precioUnitario = precioUnitario.setScale(2, RoundingMode.HALF_UP);
        this.cantidadDisponible = stock;
    }

    public String getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(String idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public Integer getCantidadDisponible() {
        return cantidadDisponible;
    }

    public String getImagenId() {
        return imagenId;
    }

    public String getEstado() {
        return estado;
    }

    public Instant getFechaRegistro() {
        return fechaRegistro;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }
}
