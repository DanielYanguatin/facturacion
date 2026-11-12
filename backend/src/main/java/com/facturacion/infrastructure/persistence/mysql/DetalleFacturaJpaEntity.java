package com.facturacion.infrastructure.persistence.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_factura")
public class DetalleFacturaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_factura", nullable = false)
    private FacturaJpaEntity factura;

    @Column(name = "id_producto", nullable = false, length = 64)
    private String idProducto;

    @Column(name = "nombre_producto_snapshot", nullable = false, length = 200)
    private String nombreProductoSnapshot;

    @Column(name = "categoria_producto_snapshot", length = 100)
    private String categoriaProductoSnapshot;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "total_producto", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalProducto;

    public DetalleFacturaJpaEntity() {
    }

    public DetalleFacturaJpaEntity(
            Long idDetalle,
            FacturaJpaEntity factura,
            String idProducto,
            String nombreProductoSnapshot,
            String categoriaProductoSnapshot,
            BigDecimal precioUnitario,
            Integer cantidad,
            BigDecimal totalProducto
    ) {
        this.idDetalle = idDetalle;
        this.factura = factura;
        this.idProducto = idProducto;
        this.nombreProductoSnapshot = nombreProductoSnapshot;
        this.categoriaProductoSnapshot = categoriaProductoSnapshot;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.totalProducto = totalProducto;
    }

    public Long getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Long idDetalle) {
        this.idDetalle = idDetalle;
    }

    public FacturaJpaEntity getFactura() {
        return factura;
    }

    public void setFactura(FacturaJpaEntity factura) {
        this.factura = factura;
    }

    public String getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(String idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProductoSnapshot() {
        return nombreProductoSnapshot;
    }

    public void setNombreProductoSnapshot(String nombreProductoSnapshot) {
        this.nombreProductoSnapshot = nombreProductoSnapshot;
    }

    public String getCategoriaProductoSnapshot() {
        return categoriaProductoSnapshot;
    }

    public void setCategoriaProductoSnapshot(String categoriaProductoSnapshot) {
        this.categoriaProductoSnapshot = categoriaProductoSnapshot;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getTotalProducto() {
        return totalProducto;
    }

    public void setTotalProducto(BigDecimal totalProducto) {
        this.totalProducto = totalProducto;
    }
}
