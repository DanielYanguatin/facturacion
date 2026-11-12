package com.facturacion.infrastructure.persistence.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataMongoProductoRepository extends MongoRepository<ProductoDocument, String> {
    long countByEstadoIgnoreCase(String estado);

    List<ProductoDocument> findByEstadoIgnoreCase(String estado);
}
