"""Script de verificacion individual de conectividad y esquemas para Fase 2."""

import os
import sys
from pathlib import Path
from dotenv import load_dotenv


def load_environment() -> Path:
    root_dir = Path(__file__).resolve().parent.parent
    env_path = root_dir / ".env"
    if not env_path.exists():
        print(f"[ERROR] No se encontro el archivo {env_path}.")
        print("Copia '.env.example' a '.env' y configura las credenciales locales.")
        sys.exit(1)
    load_dotenv(dotenv_path=env_path)
    return env_path


def check_cassandra() -> bool:
    print("\n--- 1. Verificando Apache Cassandra (Clientes) ---")
    try:
        from cassandra.cluster import Cluster

        hosts = [h.strip() for h in os.getenv("CASSANDRA_HOSTS", "localhost").split(",") if h.strip()]
        port = int(os.getenv("CASSANDRA_PORT", "9042"))
        keyspace = os.getenv("CASSANDRA_KEYSPACE", "facturacion_clientes")

        cluster = Cluster(
            contact_points=hosts,
            port=port,
            connect_timeout=10,
        )
        session = cluster.connect()
        row = session.execute("SELECT release_version FROM system.local").one()
        version = row.release_version if row else "desconocida"

        session.execute(f"USE {keyspace}")
        tables_rs = session.execute(
            "SELECT table_name FROM system_schema.tables WHERE keyspace_name = %s",
            (keyspace,),
        )
        tables = sorted(r.table_name for r in tables_rs)
        cluster.shutdown()

        print(f"[OK] Conectado a Cassandra {version} en {hosts}:{port}")
        print(f"[OK] Keyspace '{keyspace}' accesible. Tablas: {tables}")
        return "clientes_por_id" in tables and "clientes_por_correo" in tables
    except Exception as exc:
        print(f"[ERROR] Fallo conexion con Cassandra: {exc}")
        return False


def check_mongodb() -> bool:
    print("\n--- 2. Verificando MongoDB (Productos y GridFS) ---")
    try:
        from pymongo import MongoClient

        host = os.getenv("MONGO_HOST", "localhost")
        port = int(os.getenv("MONGO_PORT", "27017"))
        db_name = os.getenv("MONGO_DB", "facturacion_productos")
        user = os.getenv("MONGO_USER", "")
        password = os.getenv("MONGO_PASSWORD", "")
        auth_source = os.getenv("MONGO_AUTH_SOURCE", "admin")

        client = MongoClient(
            host=host,
            port=port,
            username=user or None,
            password=password or None,
            authSource=auth_source,
            serverSelectionTimeoutMS=5000,
        )
        ping = client.admin.command("ping")
        server_info = client.server_info()
        db = client[db_name]
        collections = sorted(db.list_collection_names())
        indexes = list(db["productos"].list_indexes()) if "productos" in collections else []
        client.close()

        print(f"[OK] Conectado a MongoDB {server_info.get('version')} en {host}:{port} (ping={ping.get('ok')})")
        print(f"[OK] Base de datos '{db_name}' accesible. Colecciones: {collections}")
        print(f"[OK] Indices en 'productos': {[idx['name'] for idx in indexes]}")
        return "productos" in collections
    except Exception as exc:
        print(f"[ERROR] Fallo conexion con MongoDB: {exc}")
        return False


def check_mysql() -> bool:
    print("\n--- 3. Verificando MySQL (Facturacion Transaccional y Outbox) ---")
    try:
        import pymysql

        host = os.getenv("MYSQL_HOST", "localhost")
        port = int(os.getenv("MYSQL_PORT", "3306"))
        database = os.getenv("MYSQL_DATABASE", "facturacion_transaccional")
        user = os.getenv("MYSQL_USER", "")
        password = os.getenv("MYSQL_PASSWORD", "")

        conn = pymysql.connect(
            host=host,
            port=port,
            user=user,
            password=password,
            database=database,
            connect_timeout=5,
        )
        with conn.cursor() as cursor:
            cursor.execute("SELECT VERSION()")
            version = cursor.fetchone()[0]
            cursor.execute("SHOW TABLES")
            tables = sorted(row[0] for row in cursor.fetchall())
        conn.close()

        expected = {"facturas", "detalle_factura", "outbox_analitico"}
        print(f"[OK] Conectado a MySQL {version} en {host}:{port}")
        print(f"[OK] Base de datos '{database}' accesible. Tablas: {tables}")
        return expected.issubset(set(tables))
    except Exception as exc:
        print(f"[ERROR] Fallo conexion con MySQL: {exc}")
        return False


def check_druid() -> bool:
    print("\n--- 4. Verificando Apache Druid (Registro Analitico) ---")
    try:
        import httpx

        router_url = os.getenv("DRUID_ROUTER_URL", "http://localhost:8888").rstrip("/")
        overlord_url = os.getenv("DRUID_OVERLORD_URL", "http://localhost:8081").rstrip("/")

        with httpx.Client(timeout=10.0) as client:
            status_resp = client.get(f"{router_url}/status")
            status_resp.raise_for_status()
            version = status_resp.json().get("version", "desconocida")

            sql_resp = client.post(
                f"{router_url}/druid/v2/sql",
                json={"query": "SELECT 1 AS ok"},
            )
            sql_resp.raise_for_status()

            overlord_resp = client.get(f"{overlord_url}/status/health")
            overlord_resp.raise_for_status()

        print(f"[OK] Conectado a Apache Druid {version} (Router: {router_url})")
        print(f"[OK] Endpoint Druid SQL operativo: {sql_resp.json()}")
        print(f"[OK] Overlord saludable en {overlord_url}: {overlord_resp.text.strip()}")
        return True
    except Exception as exc:
        print(f"[ERROR] Fallo conexion con Apache Druid: {exc}")
        return False


def main() -> int:
    env_path = load_environment()
    print(f"Usando configuracion de: {env_path}")

    results = {
        "Cassandra": check_cassandra(),
        "MongoDB": check_mongodb(),
        "MySQL": check_mysql(),
        "Apache Druid": check_druid(),
    }

    print("\n=== RESUMEN DE VERIFICACION (FASE 2) ===")
    all_ok = True
    for engine, ok in results.items():
        state = "OPERATIVO" if ok else "FALLIDO"
        print(f"- {engine}: {state}")
        if not ok:
            all_ok = False

    return 0 if all_ok else 1


if __name__ == "__main__":
    sys.exit(main())
