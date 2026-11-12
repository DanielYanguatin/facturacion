# Infraestructura Multibase de Datos (Fase 2)

Este directorio contiene los scripts de inicialización, la especificación analítica de Druid y el verificador de conectividad para los cuatro motores de base de datos del sistema de facturación.

## Responsabilidades y Conexiones por Motor

| Motor | Puerto | Base de Datos / Keyspace | Responsabilidad Estricta |
| :--- | :---: | :--- | :--- |
| **Apache Cassandra 4.1** | `9042` | `facturacion_clientes` | Almacenamiento de clientes (`clientes_por_id` y `clientes_por_correo`). Modelo orientado a consultas, sin claves foráneas. |
| **MongoDB 7.0 + GridFS** | `27018` (host) → `27017` | `facturacion_productos` | Almacenamiento de productos (`productos`) e imágenes binarias en GridFS. `cantidad_disponible` es informativa en esta primera versión. |
| **MySQL 8.0** | `3306` | `facturacion_transaccional` | Persistencia transaccional ACID de `facturas`, `detalle_factura` (con snapshot histórico de precios y nombres) y `outbox_analitico`. |
| **Apache Druid 30.0.0** | `8888`, `8081`, `8082`, `8083`, `8091` | Datasource `facturas_analiticas` | Almacenamiento y consulta analítica de eventos históricos provenientes del Outbox de MySQL. |

> **Nota importante:** Apache Druid se utiliza exclusivamente para almacenamiento y consultas analíticas de los registros de facturación. **No sustituye** una copia de seguridad (*backup*) tradicional de MySQL (`mysqldump` o *snapshots* transaccionales).

## Configuración y Arranque Local

1. Copiar `.env.example` a `.env` en la raíz del proyecto y completar las contraseñas locales de MongoDB y MySQL:
   ```powershell
   Copy-Item .env.example .env
   ```
2. Asegurarse de que **Docker Desktop** esté iniciado y levantar los servicios:
   ```powershell
   docker compose up -d
   ```
3. Verificar el estado y *healthchecks* de los contenedores:
   ```powershell
   docker compose ps
   ```
4. Instalar dependencias de Python en un entorno virtual y verificar la conexión real hacia los cuatro motores:
   ```powershell
   python -m venv .venv
   .\.venv\Scripts\Activate.ps1
   pip install -r backend\requirements.txt
   python infra\verify_connections.py
   ```
