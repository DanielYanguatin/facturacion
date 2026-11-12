package com.facturacion.infrastructure.persistence.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataCassandraClientePorCorreoRepository extends CassandraRepository<ClientePorCorreoEntity, String> {
}
