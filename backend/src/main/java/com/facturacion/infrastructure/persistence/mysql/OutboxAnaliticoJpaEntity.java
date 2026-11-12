package com.facturacion.infrastructure.persistence.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_analitico")
public class OutboxAnaliticoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Long idEvento;

    @Column(name = "id_factura", nullable = false)
    private Long idFactura;

    @Column(name = "payload_json", nullable = false, columnDefinition = "json")
    private String payloadJson;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "intentos", nullable = false)
    private Integer intentos = 0;

    @Column(name = "ultimo_error", columnDefinition = "text")
    private String ultimoError;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "enviado_en")
    private LocalDateTime enviadoEn;

    public OutboxAnaliticoJpaEntity() {
    }

    public OutboxAnaliticoJpaEntity(
            Long idFactura,
            String payloadJson,
            String estado,
            LocalDateTime creadoEn
    ) {
        this.idFactura = idFactura;
        this.payloadJson = payloadJson;
        this.estado = estado;
        this.creadoEn = creadoEn;
        this.intentos = 0;
    }

    public Long getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(Long idEvento) {
        this.idEvento = idEvento;
    }

    public Long getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(Long idFactura) {
        this.idFactura = idFactura;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getIntentos() {
        return intentos;
    }

    public void setIntentos(Integer intentos) {
        this.intentos = intentos;
    }

    public String getUltimoError() {
        return ultimoError;
    }

    public void setUltimoError(String ultimoError) {
        this.ultimoError = ultimoError;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getEnviadoEn() {
        return enviadoEn;
    }

    public void setEnviadoEn(LocalDateTime enviadoEn) {
        this.enviadoEn = enviadoEn;
    }
}
