import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  FileText,
  Search,
  Eye,
  Database,
} from 'lucide-react';
import { facturaService } from '../services/facturaService';
import type { Factura } from '../types';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { AlertBanner } from '../components/AlertBanner';

export const FacturasPage: React.FC = () => {
  const [facturas, setFacturas] = useState<Factura[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Filters
  const [numeroFactura, setNumeroFactura] = useState('');
  const [fechaDesde, setFechaDesde] = useState('');
  const [fechaHasta, setFechaHasta] = useState('');

  const cargarFacturas = async () => {
    try {
      setCargando(true);
      setError(null);
      const data = await facturaService.listar({
        numeroFactura: numeroFactura || undefined,
        fechaDesde: fechaDesde || undefined,
        fechaHasta: fechaHasta || undefined,
      });
      setFacturas(data);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Error al consultar historial de facturas en MySQL.');
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => {
    cargarFacturas();
  }, []);

  const handleFiltrar = (e: React.FormEvent) => {
    e.preventDefault();
    cargarFacturas();
  };

  const handleLimpiar = () => {
    setNumeroFactura('');
    setFechaDesde('');
    setFechaHasta('');
    facturaService.listar().then(setFacturas).catch(() => {});
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
              Historial de Facturas
            </h1>
            <span className="px-2 py-0.5 bg-blue-100 text-blue-800 text-[11px] font-bold rounded flex items-center gap-1">
              <Database className="w-3 h-3" /> MySQL + Cassandra
            </span>
          </div>
          <p className="text-sm text-slate-500 mt-1">
            Persistencia transaccional en tablas <code>facturas</code> y <code>detalle_factura</code>, enriquecida con clientes de Cassandra.
          </p>
        </div>
        <Link
          to="/facturas/nueva"
          className="inline-flex items-center gap-2 px-5 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-sm font-semibold shadow-sm transition-all"
        >
          Nueva Factura
        </Link>
      </div>

      {error && <AlertBanner tipo="error" mensaje={error} onCerrar={() => setError(null)} />}

      {/* Filter Bar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
        <form onSubmit={handleFiltrar} className="grid grid-cols-1 sm:grid-cols-12 gap-3 items-end">
          <div className="sm:col-span-5">
            <label className="block text-xs font-semibold text-slate-500 uppercase mb-1">
              Número de Factura
            </label>
            <div className="relative">
              <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
              <input
                type="text"
                placeholder="Ej. FAC-20261112-..."
                value={numeroFactura}
                onChange={(e) => setNumeroFactura(e.target.value)}
                className="w-full pl-9 pr-3 py-2 border border-slate-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          </div>

          <div className="sm:col-span-3">
            <label className="block text-xs font-semibold text-slate-500 uppercase mb-1">
              Fecha Desde
            </label>
            <input
              type="date"
              value={fechaDesde}
              onChange={(e) => setFechaDesde(e.target.value)}
              className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div className="sm:col-span-3">
            <label className="block text-xs font-semibold text-slate-500 uppercase mb-1">
              Fecha Hasta
            </label>
            <input
              type="date"
              value={fechaHasta}
              onChange={(e) => setFechaHasta(e.target.value)}
              className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div className="sm:col-span-1 flex gap-1">
            <button
              type="submit"
              className="flex-1 py-2 bg-slate-800 hover:bg-slate-900 text-white rounded-lg text-xs font-semibold transition-colors cursor-pointer"
            >
              Filtrar
            </button>
            {(numeroFactura || fechaDesde || fechaHasta) && (
              <button
                type="button"
                onClick={handleLimpiar}
                className="py-2 px-2 bg-slate-200 hover:bg-slate-300 text-slate-700 rounded-lg text-xs font-semibold transition-colors cursor-pointer"
                title="Limpiar filtros"
              >
                ✕
              </button>
            )}
          </div>
        </form>
      </div>

      {/* Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
        {cargando ? (
          <LoadingSpinner texto="Consultando facturas en MySQL..." />
        ) : facturas.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                <tr>
                  <th className="px-6 py-3.5">N° Factura</th>
                  <th className="px-6 py-3.5">Cliente (Cassandra)</th>
                  <th className="px-6 py-3.5">Fecha Emisión</th>
                  <th className="px-6 py-3.5 text-center">Ítems</th>
                  <th className="px-6 py-3.5">Método</th>
                  <th className="px-6 py-3.5 text-right">Total</th>
                  <th className="px-6 py-3.5 text-right">Acción</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {facturas.map((f) => (
                  <tr key={f.idFactura} className="hover:bg-slate-50/70 transition-colors">
                    <td className="px-6 py-4 font-mono font-bold text-indigo-700">
                      {f.numeroFactura}
                    </td>
                    <td className="px-6 py-4">
                      <div className="font-semibold text-slate-900">{f.clienteNombre}</div>
                      <div className="text-xs text-slate-400">{f.clienteCorreo}</div>
                    </td>
                    <td className="px-6 py-4 text-xs text-slate-500">
                      {new Date(f.fechaFactura).toLocaleString('es-CO')}
                    </td>
                    <td className="px-6 py-4 text-center">
                      <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-slate-100 text-slate-700">
                        {f.detalles?.length || 0}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-100">
                        {f.metodoPago}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right font-black text-slate-900 font-mono">
                      $ {f.total.toLocaleString('es-CO', { minimumFractionDigits: 2 })}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <Link
                        to={`/facturas/${f.idFactura}`}
                        className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-indigo-700 bg-indigo-50 hover:bg-indigo-100 rounded-lg transition-colors"
                      >
                        <Eye className="w-3.5 h-3.5" /> Ver / Imprimir
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="text-center py-16 text-slate-400 space-y-2">
            <FileText className="w-12 h-12 mx-auto text-slate-300" />
            <p className="font-semibold text-slate-600">No se encontraron facturas registradas</p>
            <p className="text-xs">Genera tu primera factura en el módulo de facturación.</p>
          </div>
        )}
      </div>
    </div>
  );
};
