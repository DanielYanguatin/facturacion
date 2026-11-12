package com.facturacion.infrastructure.persistence.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataCassandraClientePorIdRepository extends CassandraRepository<ClientePorIdEntity, UUID> {
}
