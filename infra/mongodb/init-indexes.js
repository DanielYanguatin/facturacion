// Script de inicializacion de indices para MongoDB (Productos)
const targetDbName = process.env.MONGO_INITDB_DATABASE || "facturacion_productos";
const targetDb = db.getSiblingDB(targetDbName);

if (!targetDb.getCollectionNames().includes("productos")) {
  targetDb.createCollection("productos");
}

targetDb.productos.createIndex(
  { nombre: "text", descripcion: "text", categoria: "text" },
  { name: "idx_productos_texto" }
);

targetDb.productos.createIndex(
  { categoria: 1, estado: 1 },
  { name: "idx_productos_categoria_estado" }
);

targetDb.productos.createIndex(
  { estado: 1, fecha_registro: -1 },
  { name: "idx_productos_estado_fecha" }
);
