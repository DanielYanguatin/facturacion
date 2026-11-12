package com.facturacion.domain.model;

import com.facturacion.domain.exception.ReglaNegocioException;
import com.facturacion.domain.exception.ValidacionDominioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class Factura {

    public static final String METODO_PAGO_EFECTIVO = "EFECTIVO";
    public static final String ESTADO_EMITIDA = "EMITIDA";

    private Long idFactura;
    private String numeroFactura;
    private String idCliente;
    private LocalDateTime fechaFactura;
    private BigDecimal subtotal;
    private BigDecimal total;
    private String metodoPago;
    private String estado;
    private List<DetalleFactura> detalles;

    public Factura() {
    }

    public static Factura crearNueva(
            String numeroFactura,
            String idCliente,
            LocalDateTime fechaFactura,
            List<DetalleFactura> detalles
    ) {
        if (numeroFactura == null || numeroFactura.trim().isEmpty()) {
            throw new ValidacionDominioException("El numero de factura es obligatorio.");
        }
        if (idCliente == null || idCliente.trim().isEmpty()) {
            throw new ValidacionDominioException("La factura debe tener un cliente asociado.");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new ReglaNegocioException("La factura debe contener al menos un producto.");
        }

        BigDecimal suma = detalles.stream()
                .map(DetalleFactura::getTotalProducto)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        Factura factura = new Factura();
        factura.numeroFactura = numeroFactura.trim();
        factura.idCliente = idCliente.trim();
        factura.fechaFactura = fechaFactura == null ? LocalDateTime.now() : fechaFactura;
        factura.subtotal = suma;
        factura.total = suma; // Sin IVA ni descuentos en esta version
        factura.metodoPago = METODO_PAGO_EFECTIVO;
        factura.estado = ESTADO_EMITIDA;
        factura.detalles = List.copyOf(detalles);
        return factura;
    }

    public static Factura reconstruir(
            Long idFactura,
            String numeroFactura,
            String idCliente,
            LocalDateTime fechaFactura,
            BigDecimal subtotal,
            BigDecimal total,
            String metodoPago,
            String estado,
            List<DetalleFactura> detalles
    ) {
        Factura factura = new Factura();
        factura.idFactura = idFactura;
        factura.numeroFactura = numeroFactura;
        factura.idCliente = idCliente;
        factura.fechaFactura = fechaFactura;
        factura.subtotal = subtotal;
        factura.total = total;
        factura.metodoPago = metodoPago;
        factura.estado = estado;
        factura.detalles = detalles == null ? Collections.emptyList() : List.copyOf(detalles);
        return factura;
    }

    public Long getIdFactura() {
        return idFactura;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public LocalDateTime getFechaFactura() {
        return fechaFactura;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public String getEstado() {
        return estado;
    }

    public List<DetalleFactura> getDetalles() {
        return detalles;
    }
}
