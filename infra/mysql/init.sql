-- Inicializacion de base de datos transaccional en MySQL
CREATE DATABASE IF NOT EXISTS facturacion_transaccional
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE facturacion_transaccional;

-- Tabla de facturas (sin clave foranea hacia Cassandra)
CREATE TABLE IF NOT EXISTS facturas (
    id_factura BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_factura VARCHAR(30) NOT NULL UNIQUE,
    id_cliente VARCHAR(36) NOT NULL COMMENT 'Referencia externa al UUID del cliente en Cassandra',
    fecha_factura DATETIME NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL,
    total DECIMAL(12, 2) NOT NULL,
    metodo_pago VARCHAR(20) NOT NULL DEFAULT 'EFECTIVO',
    estado VARCHAR(20) NOT NULL DEFAULT 'EMITIDA',
    INDEX idx_facturas_cliente (id_cliente),
    INDEX idx_facturas_fecha (fecha_factura),
    CONSTRAINT chk_facturas_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_facturas_total CHECK (total >= 0)
) ENGINE=InnoDB;

-- Tabla de detalles de factura (sin clave foranea hacia MongoDB, conserva snapshot historico)
CREATE TABLE IF NOT EXISTS detalle_factura (
    id_detalle BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_factura BIGINT NOT NULL,
    id_producto VARCHAR(64) NOT NULL COMMENT 'Referencia externa al ID del producto en MongoDB',
    nombre_producto_snapshot VARCHAR(200) NOT NULL,
    categoria_producto_snapshot VARCHAR(100) NULL,
    precio_unitario DECIMAL(12, 2) NOT NULL,
    cantidad INT NOT NULL,
    total_producto DECIMAL(12, 2) NOT NULL,
    INDEX idx_detalle_factura (id_factura),
    INDEX idx_detalle_producto (id_producto),
    CONSTRAINT fk_detalle_factura
        FOREIGN KEY (id_factura) REFERENCES facturas (id_factura)
        ON DELETE CASCADE,
    CONSTRAINT chk_detalle_precio CHECK (precio_unitario >= 0),
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_total CHECK (total_producto >= 0)
) ENGINE=InnoDB;

-- Tabla Outbox para garantizar entrega eventual de registros analiticos hacia Apache Druid
CREATE TABLE IF NOT EXISTS outbox_analitico (
    id_evento BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_factura BIGINT NOT NULL,
    payload_json JSON NOT NULL,
    estado ENUM('PENDIENTE', 'ENVIADO', 'ERROR') NOT NULL DEFAULT 'PENDIENTE',
    intentos INT NOT NULL DEFAULT 0,
    ultimo_error TEXT NULL,
    creado_en DATETIME NOT NULL,
    enviado_en DATETIME NULL,
    INDEX idx_outbox_estado (estado, creado_en),
    CONSTRAINT fk_outbox_factura
        FOREIGN KEY (id_factura) REFERENCES facturas (id_factura)
        ON DELETE CASCADE
) ENGINE=InnoDB;
