package com.facturacion.infrastructure.persistence.mysql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataFacturaRepository extends JpaRepository<FacturaJpaEntity, Long> {

    Optional<FacturaJpaEntity> findByNumeroFactura(String numeroFactura);

    @Query("""
            SELECT f FROM FacturaJpaEntity f
            WHERE (:numeroFactura IS NULL OR LOWER(f.numeroFactura) LIKE LOWER(CONCAT('%', :numeroFactura, '%')))
              AND (:idCliente IS NULL OR f.idCliente = :idCliente)
              AND (:fechaDesde IS NULL OR f.fechaFactura >= :fechaDesde)
              AND (:fechaHasta IS NULL OR f.fechaFactura <= :fechaHasta)
            ORDER BY f.fechaFactura DESC
            """)
    List<FacturaJpaEntity> buscarConFiltros(
            @Param("numeroFactura") String numeroFactura,
            @Param("idCliente") String idCliente,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta
    );

    @Query("SELECT COALESCE(SUM(f.total), 0) FROM FacturaJpaEntity f")
    BigDecimal sumarTotalVentas();
}
