import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  Printer,
  ArrowLeft,
  Receipt,
  Image as ImageIcon,
  CheckCircle2,
} from 'lucide-react';
import { facturaService } from '../services/facturaService';
import { getImagenUrl } from '../services/api';
import type { Factura } from '../types';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { AlertBanner } from '../components/AlertBanner';

export const FacturaDetallePage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [factura, setFactura] = useState<Factura | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const cargarFactura = async () => {
      if (!id) return;
      try {
        setCargando(true);
        setError(null);
        const data = await facturaService.obtenerPorId(parseInt(id, 10));
        setFactura(data);
      } catch (err: any) {
        setError(err?.response?.data?.message || 'Error al obtener la factura.');
      } finally {
        setCargando(false);
      }
    };
    cargarFactura();
  }, [id]);

  const handleImprimir = () => {
    window.print();
  };

  if (cargando) return <LoadingSpinner texto="Cargando detalle de la factura..." />;
  if (error || !factura) {
    return (
      <div className="max-w-2xl mx-auto space-y-4">
        <AlertBanner tipo="error" mensaje={error || 'Factura no encontrada'} />
        <Link
          to="/facturas"
          className="inline-flex items-center gap-2 text-sm text-indigo-600 hover:underline"
        >
          <ArrowLeft className="w-4 h-4" /> Volver al historial
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Action Bar (hidden on print) */}
      <div className="flex items-center justify-between no-print bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
        <Link
          to="/facturas"
          className="inline-flex items-center gap-2 text-sm font-semibold text-slate-600 hover:text-slate-900 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" /> Volver al historial
        </Link>
        <button
          onClick={handleImprimir}
          className="inline-flex items-center gap-2 px-5 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-sm font-semibold shadow-xs transition-all cursor-pointer"
        >
          <Printer className="w-4 h-4" /> Imprimir Factura
        </button>
      </div>

      {/* Printable Invoice Container */}
      <div className="bg-white rounded-3xl p-8 sm:p-12 border border-slate-200 shadow-sm space-y-8 print:border-none print:shadow-none print:p-0">
        {/* Invoice Header */}
        <div className="flex flex-col sm:flex-row justify-between items-start gap-6 border-b border-slate-200 pb-8">
          <div>
            <div className="flex items-center gap-2.5">
              <div className="p-2 bg-indigo-600 text-white rounded-xl">
                <Receipt className="w-6 h-6" />
              </div>
              <div>
                <h1 className="text-xl font-black text-slate-900 tracking-tight">
                  FACTURA DE VENTA
                </h1>
                <p className="text-xs text-slate-500">Sistema Multi-BD NoSQL & SQL</p>
              </div>
            </div>
            <div className="mt-4 text-xs text-slate-500 space-y-0.5">
              <p className="font-semibold text-slate-700">Facturación Académica Universitaria</p>
              <p>Asignatura: Bases de Datos No Relacionales</p>
              <p>Régimen Simplificado - Solo Efectivo</p>
            </div>
          </div>

          <div className="text-right sm:text-right w-full sm:w-auto p-4 bg-slate-50 rounded-2xl border border-slate-100">
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider block">
              Número de Factura
            </span>
            <span className="text-xl font-black text-indigo-700 font-mono block mt-0.5">
              {factura.numeroFactura}
            </span>
            <div className="mt-2 text-xs text-slate-500 space-y-0.5">
              <p>
                <span className="font-semibold">Fecha:</span>{' '}
                {new Date(factura.fechaFactura).toLocaleDateString('es-CO')}
              </p>
              <p>
                <span className="font-semibold">Hora:</span>{' '}
                {new Date(factura.fechaFactura).toLocaleTimeString('es-CO')}
              </p>
              <p>
                <span className="font-semibold">Estado:</span>{' '}
                <span className="text-emerald-700 font-bold">{factura.estado}</span>
              </p>
            </div>
          </div>
        </div>

        {/* Client & Payment Info Block */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 p-6 bg-slate-50/70 rounded-2xl border border-slate-100">
          <div>
            <span className="text-[11px] font-bold text-indigo-700 uppercase tracking-wider block mb-2">
              Datos del Cliente (Apache Cassandra)
            </span>
            <div className="text-sm space-y-1">
              <p className="font-bold text-slate-900 text-base">{factura.clienteNombre}</p>
              <p className="text-slate-600">
                <span className="text-slate-400 text-xs">Ciudad:</span> {factura.clienteCiudad}
              </p>
              <p className="text-slate-600">
                <span className="text-slate-400 text-xs">Correo:</span> {factura.clienteCorreo}
              </p>
              <p className="text-slate-600">
                <span className="text-slate-400 text-xs">Teléfono:</span> {factura.clienteTelefono}
              </p>
              {factura.clienteCategoria && (
                <p className="text-slate-600">
                  <span className="text-slate-400 text-xs">Categoría:</span>{' '}
                  <span className="font-semibold text-indigo-600">{factura.clienteCategoria}</span>
                </p>
              )}
            </div>
          </div>

          <div className="sm:border-l sm:border-slate-200 sm:pl-6 space-y-3">
            <span className="text-[11px] font-bold text-slate-500 uppercase tracking-wider block">
              Condiciones de Pago
            </span>
            <div className="text-xs space-y-2">
              <div className="flex justify-between">
                <span className="text-slate-500">Método de pago:</span>
                <span className="font-bold text-slate-800">{factura.metodoPago}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Moneda:</span>
                <span className="font-semibold text-slate-800">COP (Pesos Colombianos)</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Impuestos aplicados:</span>
                <span className="font-semibold text-slate-800">Sin IVA (0%)</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Descuentos:</span>
                <span className="font-semibold text-slate-800">$ 0.00</span>
              </div>
            </div>
          </div>
        </div>

        {/* Invoice Items Table */}
        <div>
          <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-3">
            Detalle de Productos Facturados (MySQL + MongoDB Snapshot)
          </h3>
          <div className="border border-slate-200 rounded-2xl overflow-hidden">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-500 uppercase">
                <tr>
                  <th className="px-6 py-3.5">Producto</th>
                  <th className="px-6 py-3.5 text-center">Categoría</th>
                  <th className="px-6 py-3.5 text-right">Precio Unit. (Snapshot)</th>
                  <th className="px-6 py-3.5 text-center">Cant.</th>
                  <th className="px-6 py-3.5 text-right">Total</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {factura.detalles.map((det) => (
                  <tr key={det.idDetalle}>
                    <td className="px-6 py-4 flex items-center gap-3">
                      <div className="w-10 h-10 rounded-lg bg-slate-100 border border-slate-200 overflow-hidden shrink-0 flex items-center justify-center no-print">
                        {det.idProducto ? (
                          <img
                            src={getImagenUrl(det.idProducto)}
                            alt={det.nombreProducto}
                            className="w-full h-full object-cover"
                            onError={(e) => {
                              (e.target as HTMLElement).style.display = 'none';
                            }}
                          />
                        ) : (
                          <ImageIcon className="w-4 h-4 text-slate-400" />
                        )}
                      </div>
                      <div>
                        <span className="font-bold text-slate-900 block">{det.nombreProducto}</span>
                        <span className="text-[10px] text-slate-400 font-mono">ID: {det.idProducto}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-center text-xs text-slate-600">
                      {det.categoriaProducto || 'General'}
                    </td>
                    <td className="px-6 py-4 text-right font-mono text-xs text-slate-700">
                      $ {det.precioUnitario.toLocaleString('es-CO', { minimumFractionDigits: 2 })}
                    </td>
                    <td className="px-6 py-4 text-center font-semibold text-slate-800">
                      {det.cantidad}
                    </td>
                    <td className="px-6 py-4 text-right font-black text-slate-900 font-mono">
                      $ {det.totalProducto.toLocaleString('es-CO', { minimumFractionDigits: 2 })}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Invoice Summary & Totals */}
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-end gap-6 pt-4 border-t border-slate-200">
          <div className="text-xs text-slate-500 space-y-1.5 max-w-sm">
            <p className="font-bold text-slate-700 flex items-center gap-1.5">
              <CheckCircle2 className="w-4 h-4 text-emerald-600" /> Factura Válida y Confirmada
            </p>
            <p>
              Esta factura conserva los snapshots históricos de precio y nombre del producto al momento de la venta.
            </p>
            <p className="text-[11px] text-slate-400">
              Evento analítico sincronizado con Apache Druid para registro histórico.
            </p>
          </div>

          <div className="w-full sm:w-80 bg-slate-50 p-6 rounded-2xl border border-slate-200 space-y-3">
            <div className="flex justify-between text-sm text-slate-600">
              <span>Subtotal:</span>
              <span className="font-mono font-semibold text-slate-800">
                $ {factura.subtotal.toLocaleString('es-CO', { minimumFractionDigits: 2 })}
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
            <div className="pt-3 border-t border-slate-200 flex justify-between items-baseline">
              <span className="text-base font-bold text-slate-900">TOTAL:</span>
              <span className="text-2xl font-black text-indigo-700 font-mono">
                $ {factura.total.toLocaleString('es-CO', { minimumFractionDigits: 2 })}
              </span>
            </div>
          </div>
        </div>

        {/* Footer info for print */}
        <div className="text-center text-[10px] text-slate-400 pt-6 border-t border-slate-100">
          <p>Factura generada electrónicamente por FacturaHexa · Persistencia Multi-Base de Datos</p>
          <p className="mt-0.5">Cassandra 4.1 | MongoDB 7.0 | MySQL 8.0 | Apache Druid 30.0</p>
        </div>
      </div>
    </div>
  );
};
