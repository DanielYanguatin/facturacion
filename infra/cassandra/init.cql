-- Keyspace para la gestion de clientes en Apache Cassandra
CREATE KEYSPACE IF NOT EXISTS facturacion_clientes
WITH replication = {
    'class': 'SimpleStrategy',
    'replication_factor': 1
};

USE facturacion_clientes;

-- Tabla principal consultada por identificador unico del cliente
CREATE TABLE IF NOT EXISTS clientes_por_id (
    id_cliente UUID PRIMARY KEY,
    nombre_completo TEXT,
    ciudad TEXT,
    correo TEXT,
    telefono TEXT,
    categoria TEXT,
    fecha_registro TIMESTAMP,
    actualizado_en TIMESTAMP
);

-- Tabla denormalizada para validar unicidad y consultar por correo electronico sin ALLOW FILTERING
CREATE TABLE IF NOT EXISTS clientes_por_correo (
    correo TEXT PRIMARY KEY,
    id_cliente UUID,
    nombre_completo TEXT
);
