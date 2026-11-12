package com.facturacion.infrastructure.persistence.mongodb;

import com.facturacion.application.ports.output.InfraestructuraNoDisponibleException;
import com.facturacion.domain.model.Producto;
import com.facturacion.domain.repository.ProductoRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class MongoProductoRepositoryAdapter implements ProductoRepository {

    private final SpringDataMongoProductoRepository mongoRepository;
    private final MongoTemplate mongoTemplate;

    public MongoProductoRepositoryAdapter(
            SpringDataMongoProductoRepository mongoRepository,
            MongoTemplate mongoTemplate
    ) {
        this.mongoRepository = mongoRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Producto guardar(Producto producto) {
        try {
            ProductoDocument document = toDocument(producto);
            ProductoDocument guardado = mongoRepository.save(document);
            return toDomain(guardado);
        } catch (DataAccessException exc) {
            throw new InfraestructuraNoDisponibleException("MongoDB", "Error al guardar producto en MongoDB", exc);
        }
    }

    @Override
    public Optional<Producto> buscarPorId(String idProducto) {
        try {
            return mongoRepository.findById(idProducto).map(this::toDomain);
        } catch (DataAccessException exc) {
            throw new InfraestructuraNoDisponibleException("MongoDB", "Error al buscar producto por ID en MongoDB", exc);
        }
    }

    @Override
    public List<Producto> buscar(String nombre, String categoria, boolean soloActivos) {
        try {
            List<Criteria> criterias = new ArrayList<>();

            if (soloActivos) {
                criterias.add(Criteria.where("estado").regex("^" + Producto.ESTADO_ACTIVO + "$", "i"));
            }
            if (nombre != null && !nombre.isBlank()) {
                criterias.add(Criteria.where("nombre").regex(Pattern.quote(nombre.trim()), "i"));
            }
            if (categoria != null && !categoria.isBlank()) {
                criterias.add(Criteria.where("categoria").regex("^" + Pattern.quote(categoria.trim()) + "$", "i"));
            }

            Query query = new Query();
            if (!criterias.isEmpty()) {
                query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[0])));
            }
            query.with(Sort.by(Sort.Direction.DESC, "fecha_registro"));

            return mongoTemplate.find(query, ProductoDocument.class).stream()
                    .map(this::toDomain)
                    .toList();
        } catch (DataAccessException exc) {
            throw new InfraestructuraNoDisponibleException("MongoDB", "Error al consultar productos en MongoDB", exc);
        }
    }

    @Override
    public long contarTotal() {
        try {
            return mongoRepository.count();
        } catch (DataAccessException exc) {
            throw new InfraestructuraNoDisponibleException("MongoDB", "Error al contar productos en MongoDB", exc);
        }
    }

    @Override
    public long contarActivos() {
        try {
            return mongoRepository.countByEstadoIgnoreCase(Producto.ESTADO_ACTIVO);
        } catch (DataAccessException exc) {
            throw new InfraestructuraNoDisponibleException("MongoDB", "Error al contar productos activos en MongoDB", exc);
        }
    }

    private ProductoDocument toDocument(Producto domain) {
        return new ProductoDocument(
                domain.getIdProducto(),
                domain.getNombre(),
                domain.getDescripcion(),
                domain.getCategoria(),
                domain.getPrecioUnitario(),
                domain.getCantidadDisponible(),
                domain.getImagenId(),
                domain.getEstado(),
                domain.getFechaRegistro(),
                domain.getActualizadoEn()
        );
    }

    private Producto toDomain(ProductoDocument doc) {
        return Producto.reconstruir(
                doc.getId(),
                doc.getNombre(),
                doc.getDescripcion(),
                doc.getCategoria(),
                doc.getPrecioUnitario(),
                doc.getCantidadDisponible(),
                doc.getImagenId(),
                doc.getEstado(),
                doc.getFechaRegistro(),
                doc.getActualizadoEn()
        );
    }
}
