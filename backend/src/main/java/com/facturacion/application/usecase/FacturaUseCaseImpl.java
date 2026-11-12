package com.facturacion.application.usecase;

import com.facturacion.application.ports.input.FacturaUseCase;
import com.facturacion.domain.exception.EntidadNoEncontradaException;
import com.facturacion.domain.exception.ReglaNegocioException;
import com.facturacion.domain.exception.ValidacionDominioException;
import com.facturacion.domain.model.Cliente;
import com.facturacion.domain.model.DetalleFactura;
import com.facturacion.domain.model.EventoAnaliticoFactura;
import com.facturacion.domain.model.Factura;
import com.facturacion.domain.model.Producto;
import com.facturacion.domain.repository.ClienteRepository;
import com.facturacion.domain.repository.FacturaRepository;
import com.facturacion.domain.repository.ProductoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FacturaUseCaseImpl implements FacturaUseCase {

    private static final DateTimeFormatter FECHA_NUM_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final FacturaRepository facturaRepository;

    public FacturaUseCaseImpl(
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository,
            FacturaRepository facturaRepository
    ) {
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.facturaRepository = facturaRepository;
    }

    @Override
    public FacturaEnriquecida generarFactura(GenerarFacturaCommand command) {
        if (command == null || command.idCliente() == null || command.idCliente().trim().isEmpty()) {
            throw new ValidacionDominioException("Debe seleccionar un cliente para generar la factura.");
        }
        if (command.items() == null || command.items().isEmpty()) {
            throw new ReglaNegocioException("Debe agregar al menos un producto para generar la factura.");
        }

        UUID clienteUuid = parsearUuidCliente(command.idCliente());
        Cliente cliente = clienteRepository.buscarPorId(clienteUuid)
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "El cliente con ID '" + command.idCliente() + "' no existe en Cassandra."
                ));

        List<DetalleFactura> detalles = new ArrayList<>();
        Map<String, String> imagenesPorProducto = new HashMap<>();

        for (ItemFacturaCommand item : command.items()) {
            if (item.idProducto() == null || item.idProducto().trim().isEmpty()) {
                throw new ValidacionDominioException("Cada linea debe indicar un ID de producto valido.");
            }
            if (item.cantidad() == null || item.cantidad() <= 0) {
                throw new ValidacionDominioException("La cantidad de cada producto debe ser mayor que cero.");
            }

            Producto producto = productoRepository.buscarPorId(item.idProducto().trim())
                    .orElseThrow(() -> new EntidadNoEncontradaException(
                            "El producto con ID '" + item.idProducto() + "' no existe en MongoDB."
                    ));

            if (!producto.estaActivo()) {
                throw new ReglaNegocioException(
                        "El producto '" + producto.getNombre() + "' esta inactivo y no puede facturarse."
                );
            }

            // Se usa siempre el precio y nombre reales consultados en MongoDB (nunca del frontend)
            DetalleFactura detalle = DetalleFactura.crear(
                    producto.getIdProducto(),
                    producto.getNombre(),
                    producto.getCategoria(),
                    producto.getPrecioUnitario(),
                    item.cantidad()
            );
            detalles.add(detalle);
            if (producto.getImagenId() != null) {
                imagenesPorProducto.put(producto.getIdProducto(), producto.getImagenId());
            }
        }

        LocalDateTime ahora = LocalDateTime.now();
        String numeroFactura = generarNumeroFactura(ahora);
        Factura nuevaFactura = Factura.crearNueva(
                numeroFactura,
                cliente.getIdCliente().toString(),
                ahora,
                detalles
        );

        String categoriaCliente = cliente.getCategoria();
        Factura guardada = facturaRepository.guardarTransaccionalConOutbox(
                nuevaFactura,
                facturaPersistida -> construirEventosAnaliticos(facturaPersistida, categoriaCliente)
        );

        return new FacturaEnriquecida(guardada, cliente, imagenesPorProducto);
    }

    @Override
    public FacturaEnriquecida obtenerPorId(Long idFactura) {
        Factura factura = facturaRepository.buscarPorId(idFactura)
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "No se encontro la factura con ID '" + idFactura + "'."
                ));
        return enriquecerFacturas(List.of(factura)).getFirst();
    }

    @Override
    public FacturaEnriquecida obtenerPorNumero(String numeroFactura) {
        Factura factura = facturaRepository.buscarPorNumero(numeroFactura)
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "No se encontro la factura con numero '" + numeroFactura + "'."
                ));
        return enriquecerFacturas(List.of(factura)).getFirst();
    }

    @Override
    public List<FacturaEnriquecida> listarFacturas(
            String numeroFactura,
            String idCliente,
            LocalDate fechaDesde,
            LocalDate fechaHasta
    ) {
        LocalDateTime desde = fechaDesde == null ? null : fechaDesde.atStartOfDay();
        LocalDateTime hasta = fechaHasta == null ? null : fechaHasta.atTime(LocalTime.MAX);
        List<Factura> facturas = facturaRepository.listar(numeroFactura, idCliente, desde, hasta);
        return enriquecerFacturas(facturas);
    }

    public List<FacturaEnriquecida> enriquecerFacturas(List<Factura> facturas) {
        Map<String, Cliente> cacheClientes = new HashMap<>();
        Map<String, String> cacheImagenes = new HashMap<>();
        List<FacturaEnriquecida> resultado = new ArrayList<>(facturas.size());

        for (Factura factura : facturas) {
            Cliente cliente = resolverCliente(factura.getIdCliente(), cacheClientes);
            Map<String, String> imagenes = new HashMap<>();
            for (DetalleFactura detalle : factura.getDetalles()) {
                String imagenId = resolverImagenProducto(detalle.getIdProducto(), cacheImagenes);
                if (imagenId != null) {
                    imagenes.put(detalle.getIdProducto(), imagenId);
                }
            }
            resultado.add(new FacturaEnriquecida(factura, cliente, imagenes));
        }
        return resultado;
    }

    private Cliente resolverCliente(String idCliente, Map<String, Cliente> cache) {
        if (idCliente == null || idCliente.isBlank()) {
            return null;
        }
        if (cache.containsKey(idCliente)) {
            return cache.get(idCliente);
        }
        Cliente cliente = null;
        try {
            cliente = clienteRepository.buscarPorId(UUID.fromString(idCliente)).orElse(null);
        } catch (IllegalArgumentException ignored) {
            cliente = null;
        }
        cache.put(idCliente, cliente);
        return cliente;
    }

    private String resolverImagenProducto(String idProducto, Map<String, String> cache) {
        if (idProducto == null || idProducto.isBlank()) {
            return null;
        }
        if (cache.containsKey(idProducto)) {
            return cache.get(idProducto);
        }
        String imagenId = productoRepository.buscarPorId(idProducto)
                .map(Producto::getImagenId)
                .orElse(null);
        cache.put(idProducto, imagenId);
        return imagenId;
    }

    private UUID parsearUuidCliente(String idCliente) {
        try {
            return UUID.fromString(idCliente.trim());
        } catch (IllegalArgumentException exc) {
            throw new ValidacionDominioException(
                    "El identificador del cliente no tiene formato UUID valido: '" + idCliente + "'."
            );
        }
    }

    private String generarNumeroFactura(LocalDateTime fecha) {
        String sufijo = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "FAC-" + fecha.format(FECHA_NUM_FORMAT) + "-" + sufijo;
    }

    private List<EventoAnaliticoFactura> construirEventosAnaliticos(
            Factura factura,
            String categoriaCliente
    ) {
        String fechaIso = factura.getFechaFactura().atOffset(ZoneOffset.UTC).toString();
        String catCliente = (categoriaCliente == null || categoriaCliente.isBlank())
                ? "Sin categoria"
                : categoriaCliente;

        return factura.getDetalles().stream()
                .map(det -> new EventoAnaliticoFactura(
                        factura.getIdFactura(),
                        factura.getNumeroFactura(),
                        fechaIso,
                        factura.getIdCliente(),
                        catCliente,
                        det.getIdProducto(),
                        det.getNombreProductoSnapshot(),
                        det.getCategoriaProductoSnapshot() == null ? "Sin categoria" : det.getCategoriaProductoSnapshot(),
                        det.getPrecioUnitario(),
                        det.getCantidad(),
                        det.getTotalProducto(),
                        factura.getTotal()
                ))
                .toList();
    }
}
