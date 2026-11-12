package com.facturacion.infrastructure.persistence.mysql;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SpringDataOutboxRepository extends JpaRepository<OutboxAnaliticoJpaEntity, Long> {

    List<OutboxAnaliticoJpaEntity> findByEstadoInOrderByCreadoEnAsc(Collection<String> estados, Pageable pageable);

    long countByEstado(String estado);

    @Query("SELECT o.estado, COUNT(o) FROM OutboxAnaliticoJpaEntity o GROUP BY o.estado")
    List<Object[]> contarAgrupadoPorEstado();
}
