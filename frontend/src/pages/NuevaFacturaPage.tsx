import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Receipt,
  UserCheck,
  Package,
  Plus,
  Trash2,
  CheckCircle,
  Database,
  Image as ImageIcon,
  AlertCircle,
} from 'lucide-react';
import { clienteService } from '../services/clienteService';
import { productoService } from '../services/productoService';
import { facturaService } from '../services/facturaService';
import { getImagenUrl } from '../services/api';
import type { Cliente, Producto } from '../types';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { AlertBanner } from '../components/AlertBanner';

interface LineaFactura {
  producto: Producto;
  cantidad: number;
}

export const NuevaFacturaPage: React.FC = () => {
  const navigate = useNavigate();

  // Master Data
  const [clientes, setClientes] = useState<Cliente[]>([]);
  const [productosDisponibles, setProductosDisponibles] = useState<Producto[]>([]);
  const [cargandoDatos, setCargandoDatos] = useState(true);

  // Form Selection
  const [clienteSeleccionadoId, setClienteSeleccionadoId] = useState<string>('');
  const [lineas, setLineas] = useState<LineaFactura[]>([]);

  // Product to add
  const [productoAgregarId, setProductoAgregarId] = useState<string>('');
  const [cantidadAgregar, setCantidadAgregar] = useState<number>(1);

  // Status
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [facturaCreada, setFacturaCreada] = useState<any | null>(null);

  useEffect(() => {
    const cargarCatalogos = async () => {
      try {
        setCargandoDatos(true);
        const [listaClientes, listaProds] = await Promise.all([
          clienteService.listar(),
          productoService.listar({ soloActivos: true }),
        ]);
        setClientes(listaClientes);
        setProductosDisponibles(listaProds);
      } catch (err: any) {
        setError(err?.response?.data?.message || 'Error al cargar clientes o productos.');
      } finally {
        setCargandoDatos(false);
      }
    };
    cargarCatalogos();
  }, []);

  const clienteActual = clientes.find((c) => c.idCliente === clienteSeleccionadoId) || null;
  const productoParaAgregar = productosDisponibles.find((p) => p.idProducto === productoAgregarId) || null;

  // Calculos automaticos en tiempo real
  const calcularTotalLinea = (linea: LineaFactura): number => {
    return linea.producto.precioUnitario * linea.cantidad;
  };

  const totalFactura = lineas.reduce((acc, linea) => acc + calcularTotalLinea(linea), 0);

  const agregarProducto = () => {
    if (!productoParaAgregar) return;
    if (cantidadAgregar <= 0) {
      setError('La cantidad del producto debe ser mayor a cero.');
      return;
    }

    // Verificar si ya existe en las lineas
    const indexExistente = lineas.findIndex((l) => l.producto.idProducto === productoParaAgregar.idProducto);
    if (indexExistente >= 0) {
      const nuevasLineas = [...lineas];
      nuevasLineas[indexExistente].cantidad += cantidadAgregar;
      setLineas(nuevasLineas);
    } else {
      setLineas([...lineas, { producto: productoParaAgregar, cantidad: cantidadAgregar }]);
    }

    setProductoAgregarId('');
    setCantidadAgregar(1);
    setError(null);
  };

  const modificarCantidad = (index: number, nuevaCantidad: number) => {
    if (nuevaCantidad <= 0) return;
    const nuevas = [...lineas];
    nuevas[index].cantidad = nuevaCantidad;
    setLineas(nuevas);
  };

  const eliminarLinea = (index: number) => {
    setLineas(lineas.filter((_, i) => i !== index));
  };

  const handleConfirmarFactura = async () => {
    if (!clienteActual) {
      setError('Debe seleccionar un cliente antes de emitir la factura.');
      return;
    }
    if (lineas.length === 0) {
      setError('Debe agregar al menos un producto a la factura.');
      return;
    }

    try {
      setGuardando(true);
      setError(null);

      const payload = {
        idCliente: clienteActual.idCliente,
        items: lineas.map((l) => ({
          idProducto: l.producto.idProducto,
          cantidad: l.cantidad,
        })),
      };

      const factura = await facturaService.crear(payload);
      setFacturaCreada(factura);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Error al generar la factura transaccional en MySQL.');
    } finally {
      setGuardando(false);
    }
  };

  if (cargandoDatos) {
    return <LoadingSpinner texto="Cargando clientes (Cassandra) y productos (MongoDB)..." />;
  }

  if (facturaCreada) {
    return (
      <div className="max-w-2xl mx-auto bg-white rounded-3xl p-8 border border-slate-200 shadow-lg text-center space-y-6 my-12 animate-in zoom-in-95 duration-200">
        <div className="w-16 h-16 bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center mx-auto">
          <CheckCircle className="w-10 h-10" />
        </div>
        <div>
          <span className="text-xs font-bold text-emerald-600 uppercase tracking-wider">
            Transacción MySQL Confirmada (ACID)
          </span>
          <h2 className="text-2xl font-bold text-slate-900 mt-1">
            Factura {facturaCreada.numeroFactura} Generada con Éxito
          </h2>
          <p className="text-sm text-slate-500 mt-2">
            La factura y sus detalles se persistieron en MySQL, y el registro analítico fue programado en el Outbox para Apache Druid.
          </p>
        </div>

        <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100 text-left space-y-2 text-sm">
          <div className="flex justify-between">
            <span className="text-slate-500 font-medium">Cliente:</span>
            <span className="font-semibold text-slate-800">{facturaCreada.clienteNombre}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-500 font-medium">Método de pago:</span>
            <span className="font-semibold text-slate-800">{facturaCreada.metodoPago}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-500 font-medium">Líneas de producto:</span>
            <span className="font-semibold text-slate-800">{facturaCreada.detalles?.length || 0}</span>
          </div>
          <div className="flex justify-between pt-2 border-t border-slate-200 text-base font-bold text-indigo-700">
            <span>Total Factura:</span>
            <span>$ {facturaCreada.total.toLocaleString('es-CO', { minimumFractionDigits: 2 })}</span>
          </div>
        </div>

        <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-4">
          <button
            onClick={() => navigate(`/facturas/${facturaCreada.idFactura}`)}
            className="w-full sm:w-auto px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-sm font-semibold shadow-sm transition-all cursor-pointer"
          >
            Ver e Imprimir Factura
          </button>
          <button
            onClick={() => {
              setFacturaCreada(null);
              setClienteSeleccionadoId('');
              setLineas([]);
            }}
            className="w-full sm:w-auto px-6 py-2.5 border border-slate-300 text-slate-700 hover:bg-slate-50 rounded-xl text-sm font-medium transition-all cursor-pointer"
          >
            Crear Otra Factura
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto space-y-8">
      {/* Header */}
      <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
              Generar Factura
            </h1>
            <span className="px-2.5 py-0.5 bg-indigo-100 text-indigo-800 text-[11px] font-bold rounded flex items-center gap-1">
              <Database className="w-3 h-3" /> Transaccional MySQL
            </span>
          </div>
          <p className="text-sm text-slate-500 mt-1">
            Módulo principal: consulta datos desde Cassandra y MongoDB, ejecuta transacción ACID en MySQL y despacha eventos a Druid.
          </p>
        </div>
        <div className="text-xs text-slate-500 bg-slate-50 px-3.5 py-2 rounded-xl border border-slate-200">
          Reglas: Solo Efectivo · Sin IVA · Precios protegidos de BD
        </div>
      </div>

      {error && <AlertBanner tipo="error" mensaje={error} onCerrar={() => setError(null)} />}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left 2 Cols: Form Client & Products Selection */}
        <div className="lg:col-span-2 space-y-6">
          {/* Step 1: Seleccionar Cliente */}
          <div className="bg-white rounded-2xl p-6 border border-slate-200 shadow-xs space-y-4">
            <div className="flex items-center gap-2 text-sm font-bold text-slate-800 uppercase tracking-wider">
              <UserCheck className="w-4 h-4 text-indigo-600" />
              1. Seleccionar Cliente (Datos de Cassandra)
            </div>

            <div>
              <select
                value={clienteSeleccionadoId}
                onChange={(e) => setClienteSeleccionadoId(e.target.value)}
                className="w-full px-4 py-2.5 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-indigo-500 bg-white"
              >
                <option value="">-- Seleccionar cliente previamente registrado --</option>
                {clientes.map((c) => (
                  <option key={c.idCliente} value={c.idCliente}>
                    {c.nombreCompleto} ({c.ciudad}) - {c.correo}
                  </option>
                ))}
              </select>
            </div>

            {/* Client preview card */}
            {clienteActual ? (
              <div className="p-4 bg-indigo-50/50 rounded-xl border border-indigo-100 text-xs grid grid-cols-2 sm:grid-cols-4 gap-3">
                <div>
                  <span className="font-semibold text-slate-400 block uppercase text-[10px]">Cliente</span>
                  <span className="font-bold text-slate-800 text-sm">{clienteActual.nombreCompleto}</span>
                </div>
                <div>
                  <span className="font-semibold text-slate-400 block uppercase text-[10px]">Ciudad</span>
                  <span className="text-slate-700 font-medium">{clienteActual.ciudad}</span>
                </div>
                <div>
                  <span className="font-semibold text-slate-400 block uppercase text-[10px]">Correo</span>
                  <span className="text-slate-700 font-medium truncate block">{clienteActual.correo}</span>
                </div>
                <div>
                  <span className="font-semibold text-slate-400 block uppercase text-[10px]">Categoría</span>
                  <span className="font-semibold text-indigo-700">{clienteActual.categoria || 'Regular'}</span>
                </div>
              </div>
            ) : (
              <p className="text-xs text-slate-400 italic">
                Seleccione un cliente para visualizar sus datos de Cassandra y asociarlo a la factura.
              </p>
            )}
          </div>

          {/* Step 2: Agregar Productos */}
          <div className="bg-white rounded-2xl p-6 border border-slate-200 shadow-xs space-y-4">
            <div className="flex items-center gap-2 text-sm font-bold text-slate-800 uppercase tracking-wider">
              <Package className="w-4 h-4 text-indigo-600" />
              2. Agregar Productos (Catálogo de MongoDB)
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-12 gap-3 items-end">
              <div className="sm:col-span-7">
                <label className="block text-xs font-semibold text-slate-500 uppercase mb-1">
                  Producto
                </label>
                <select
                  value={productoAgregarId}
                  onChange={(e) => setProductoAgregarId(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 bg-white"
                >
                  <option value="">-- Seleccionar producto activo --</option>
                  {productosDisponibles.map((p) => (
                    <option key={p.idProducto} value={p.idProducto}>
                      {p.nombre} (${p.precioUnitario.toLocaleString('es-CO')} - {p.categoria})
                    </option>
                  ))}
                </select>
              </div>

              <div className="sm:col-span-3">
                <label className="block text-xs font-semibold text-slate-500 uppercase mb-1">
                  Cantidad
                </label>
                <input
                  type="number"
                  min="1"
                  value={cantidadAgregar}
                  onChange={(e) => setCantidadAgregar(parseInt(e.target.value, 10) || 1)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
                />
              </div>

              <div className="sm:col-span-2">
                <button
                  type="button"
                  disabled={!productoAgregarId}
                  onClick={agregarProducto}
                  className="w-full py-2 px-3 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white rounded-lg text-sm font-semibold flex items-center justify-center gap-1 transition-colors cursor-pointer"
                >
                  <Plus className="w-4 h-4" /> Agregar
                </button>
              </div>
            </div>

            {/* Preview of selected product to add */}
            {productoParaAgregar && (
              <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 flex items-center gap-4 text-xs">
                <div className="w-12 h-12 bg-white rounded-lg border border-slate-200 overflow-hidden shrink-0 flex items-center justify-center">
                  {productoParaAgregar.imagenId ? (
                    <img
                      src={getImagenUrl(productoParaAgregar.idProducto)}
                      alt={productoParaAgregar.nombre}
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <ImageIcon className="w-6 h-6 text-slate-300" />
                  )}
                </div>
                <div className="flex-1">
                  <p className="font-bold text-slate-800 text-sm">{productoParaAgregar.nombre}</p>
                  <p className="text-slate-500">{productoParaAgregar.categoria}</p>
                </div>
                <div className="text-right">
                  <span className="text-[10px] text-slate-400 block uppercase">Precio Unitario</span>
                  <span className="font-bold text-indigo-700 text-sm">
                    $ {productoParaAgregar.precioUnitario.toLocaleString('es-CO', { minimumFractionDigits: 2 })}
                  </span>
                </div>
              </div>
            )}

            {/* Added Lines Table */}
            <div className="pt-4 border-t border-slate-100">
              <h4 className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-3">
                Líneas de la Factura ({lineas.length})
              </h4>

              {lineas.length > 0 ? (
                <div className="border border-slate-200 rounded-xl overflow-hidden">
                  <table className="w-full text-left text-sm">
                    <thead className="bg-slate-50 text-xs font-semibold text-slate-500 uppercase border-b border-slate-200">
                      <tr>
                        <th className="px-4 py-2.5">Producto</th>
                        <th className="px-4 py-2.5 text-right">Precio Unit.</th>
                        <th className="px-4 py-2.5 text-center">Cantidad</th>
                        <th className="px-4 py-2.5 text-right">Total</th>
                        <th className="px-4 py-2.5 text-center">Quitar</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {lineas.map((linea, index) => {
                        const totalLinea = calcularTotalLinea(linea);
                        return (
                          <tr key={linea.producto.idProducto} className="hover:bg-slate-50/50">
                            <td className="px-4 py-3 flex items-center gap-3">
                              <div className="w-10 h-10 bg-slate-100 rounded-md overflow-hidden shrink-0 flex items-center justify-center border border-slate-200">
                                {linea.producto.imagenId ? (
                                  <img
                                    src={getImagenUrl(linea.producto.idProducto)}
                                    alt={linea.producto.nombre}
                                    className="w-full h-full object-cover"
                                  />
                                ) : (
                                  <ImageIcon className="w-4 h-4 text-slate-400" />
                                )}
                              </div>
                              <div>
                                <p className="font-semibold text-slate-800 text-xs">
                                  {linea.producto.nombre}
                                </p>
                                <span className="text-[10px] text-slate-400">
                                  {linea.producto.categoria}
                                </span>
                              </div>
                            </td>
                            <td className="px-4 py-3 text-right font-mono text-xs">
                              $ {linea.producto.precioUnitario.toLocaleString('es-CO', { minimumFractionDigits: 2 })}
                            </td>
                            <td className="px-4 py-3 text-center">
                              <input
                                type="number"
                                min="1"
                                value={linea.cantidad}
                                onChange={(e) => modificarCantidad(index, parseInt(e.target.value, 10) || 1)}
                                className="w-16 px-2 py-1 border border-slate-300 rounded text-center text-xs font-semibold focus:ring-1 focus:ring-indigo-500"
                              />
                            </td>
                            <td className="px-4 py-3 text-right font-bold text-slate-900 text-xs">
                              $ {totalLinea.toLocaleString('es-CO', { minimumFractionDigits: 2 })}
                            </td>
                            <td className="px-4 py-3 text-center">
                              <button
                                type="button"
                                onClick={() => eliminarLinea(index)}
                                className="text-slate-400 hover:text-rose-500 p-1 rounded transition-colors cursor-pointer"
                              >
                                <Trash2 className="w-4 h-4" />
                              </button>
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              ) : (
                <div className="p-8 text-center bg-slate-50 rounded-xl border border-dashed border-slate-200 text-slate-400 text-xs">
                  No has agregado productos a la factura todavía.
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Right Col: Invoice Summary Card */}
        <div className="space-y-6">
          <div className="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm sticky top-24 space-y-6">
            <div className="flex items-center gap-2 border-b border-slate-100 pb-4">
              <Receipt className="w-5 h-5 text-indigo-600" />
              <h3 className="font-bold text-lg text-slate-900">Resumen de Factura</h3>
            </div>

            <div className="space-y-3 text-sm">
              <div className="flex justify-between text-slate-600">
                <span>Cliente:</span>
                <span className="font-semibold text-slate-800">
                  {clienteActual ? clienteActual.nombreCompleto : 'Sin seleccionar'}
                </span>
              </div>
              <div className="flex justify-between text-slate-600">
                <span>Método de Pago:</span>
                <span className="font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded text-xs">
                  EFECTIVO (Fijo)
                </span>
              </div>
              <div className="flex justify-between text-slate-600">
                <span>Productos agregados:</span>
                <span className="font-semibold text-slate-800">{lineas.length}</span>
              </div>
              <div className="flex justify-between text-slate-600">
                <span>Subtotal:</span>
                <span className="font-mono text-slate-800 font-semibold">
                  $ {totalFactura.toLocaleString('es-CO', { minimumFractionDigits: 2 })}
                </span>
              </div>
              <div className="flex justify-between text-xs text-slate-400">
                <span>IVA (0%):</span>
                <span>$ 0.00</span>
              </div>
              <div className="flex justify-between text-xs text-slate-400">
                <span>Descuento:</span>
                <span>$ 0.00</span>
              </div>

              <div className="pt-4 border-t border-slate-200 flex justify-between items-baseline">
                <span className="text-base font-bold text-slate-900">TOTAL FACTURA:</span>
                <span className="text-2xl font-black text-indigo-700 font-mono">
                  $ {totalFactura.toLocaleString('es-CO', { minimumFractionDigits: 2 })}
                </span>
              </div>
            </div>

            <div className="pt-2">
              <button
                type="button"
                disabled={guardando || !clienteActual || lineas.length === 0}
                onClick={handleConfirmarFactura}
                className="w-full py-3.5 px-4 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white rounded-xl text-base font-bold shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2 cursor-pointer"
              >
                {guardando ? (
                  'Registrando en MySQL...'
                ) : (
                  <>
                    <CheckCircle className="w-5 h-5" /> Confirmar Factura
                  </>
                )}
              </button>
            </div>

            <div className="text-[11px] text-slate-400 space-y-1 bg-slate-50 p-3 rounded-lg border border-slate-100">
              <p className="flex items-center gap-1 font-semibold text-slate-600">
                <AlertCircle className="w-3.5 h-3.5 text-indigo-500" /> Garantías de consistencia:
              </p>
              <p>• Transacción ACID en MySQL para factura y detalle.</p>
              <p>• Snapshots históricos de nombres y precios.</p>
              <p>• Registro en Outbox analítico para Apache Druid.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
