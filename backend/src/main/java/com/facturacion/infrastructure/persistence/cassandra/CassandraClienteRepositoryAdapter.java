package com.facturacion.infrastructure.persistence.cassandra;

import com.facturacion.application.ports.output.InfraestructuraNoDisponibleException;
import com.facturacion.domain.model.Cliente;
import com.facturacion.domain.repository.ClienteRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CassandraClienteRepositoryAdapter implements ClienteRepository {

    private final SpringDataCassandraClientePorIdRepository porIdRepo;
    private final SpringDataCassandraClientePorCorreoRepository porCorreoRepo;

    public CassandraClienteRepositoryAdapter(
            SpringDataCassandraClientePorIdRepository porIdRepo,
            SpringDataCassandraClientePorCorreoRepository porCorreoRepo
    ) {
        this.porIdRepo = porIdRepo;
        this.porCorreoRepo = porCorreoRepo;
    }

    @Override
    public Cliente guardarNuevo(Cliente cliente) {
        try {
            ClientePorIdEntity entityId = toEntityId(cliente);
            ClientePorCorreoEntity entityCorreo = new ClientePorCorreoEntity(
                    cliente.getCorreo(),
                    cliente.getIdCliente(),
                    cliente.getNombreCompleto()
            );

            porIdRepo.save(entityId);
            porCorreoRepo.save(entityCorreo);
            return cliente;
        } catch (DataAccessException exc) {
            throw new InfraestructuraNoDisponibleException("Cassandra", "Error al guardar cliente en Cassandra", exc);
        }
    }

    @Override
    public Cliente actualizar(Cliente cliente, String correoAnterior) {
        try {
            ClientePorIdEntity entityId = toEntityId(cliente);
            porIdRepo.save(entityId);

            if (correoAnterior != null && !correoAnterior.equalsIgnoreCase(cliente.getCorreo())) {
                porCorreoRepo.deleteById(correoAnterior.toLowerCase());
            }

            ClientePorCorreoEntity entityCorreo = new ClientePorCorreoEntity(
                    cliente.getCorreo(),
                    cliente.getIdCliente(),
                    cliente.getNombreCompleto()
            );
            porCorreoRepo.save(entityCorreo);
            return cliente;
        } catch (DataAccessException exc) {
            throw new InfraestructuraNoDisponibleException("Cassandra", "Error al actualizar cliente en Cassandra", exc);
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(UUID idCliente) {
        try {
            return porIdRepo.findById(idCliente).map(this::toDomain);
        } catch (DataAccessException exc) {
            throw new InfraestructuraNoDisponibleException("Cassandra", "Error al buscar cliente por ID en Cassandra", exc);
        }
    }

    @Override
    public Optional<Cliente> buscarPorCorreo(String correo) {
        try {
            if (correo == null || correo.isBlank()) {
                return Optional.empty();
            }
            return porCorreoRepo.findById(correo.trim().toLowerCase())
                    .flatMap(idx -> porIdRepo.findById(idx.getIdCliente()))
                    .map(this::toDomain);
        } catch (DataAccessException exc) {
            throw new InfraestructuraNoDisponibleException("Cassandra", "Error al buscar cliente por correo en Cassandra", exc);
        }
    }

    @Override
    public List<Cliente> listarTodos() {
        try {
            return porIdRepo.findAll().stream().map(this::toDomain).toList();
        } catch (DataAccessException exc) {
            throw new InfraestructuraNoDisponibleException("Cassandra", "Error al listar clientes en Cassandra", exc);
        }
    }

    @Override
    public long contarTotal() {
        try {
            return porIdRepo.count();
        } catch (DataAccessException exc) {
            throw new InfraestructuraNoDisponibleException("Cassandra", "Error al contar clientes en Cassandra", exc);
        }
    }

    private ClientePorIdEntity toEntityId(Cliente domain) {
        return new ClientePorIdEntity(
                domain.getIdCliente(),
                domain.getNombreCompleto(),
                domain.getCiudad(),
                domain.getCorreo(),
                domain.getTelefono(),
                domain.getCategoria(),
                domain.getFechaRegistro(),
                domain.getActualizadoEn()
        );
    }

    private Cliente toDomain(ClientePorIdEntity entity) {
        return Cliente.reconstruir(
                entity.getIdCliente(),
                entity.getNombreCompleto(),
                entity.getCiudad(),
                entity.getCorreo(),
                entity.getTelefono(),
                entity.getCategoria(),
                entity.getFechaRegistro(),
                entity.getActualizadoEn()
        );
    }
}
