import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Users,
  Package,
  Receipt,
  DollarSign,
  TrendingUp,
  PlusCircle,
  ExternalLink,
} from 'lucide-react';
import { dashboardService } from '../services/dashboardService';
import type { DashboardResumen } from '../types';
import { StatCard } from '../components/StatCard';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { AlertBanner } from '../components/AlertBanner';

export const DashboardPage: React.FC = () => {
  const [resumen, setResumen] = useState<DashboardResumen | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const cargarResumen = async () => {
    try {
      setCargando(true);
      setError(null);
      const data = await dashboardService.obtenerResumen();
      setResumen(data);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Error al conectar con los servicios de base de datos.');
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => {
    cargarResumen();
  }, []);

  if (cargando) return <LoadingSpinner texto="Consultando métricas multibase de datos..." />;

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      {/* Top Banner / Actions */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
            Panel Principal
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Persistencia distribuida en Cassandra (Clientes), MongoDB (Productos), MySQL (Facturas) y Druid (Analítica).
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Link
            to="/facturas/nueva"
            className="inline-flex items-center gap-2 px-5 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-sm font-semibold shadow-sm transition-all"
          >
            <PlusCircle className="w-4 h-4" />
            Nueva Factura
          </Link>
        </div>
      </div>

      {error && <AlertBanner tipo="error" mensaje={error} />}

      {/* Stats Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard
          titulo="Clientes Registrados"
          valor={resumen?.totalClientes ?? 0}
          subtitulo="Almacenados en Apache Cassandra"
          icono={Users}
          color="indigo"
        />
        <StatCard
          titulo="Productos en Catálogo"
          valor={resumen?.totalProductos ?? 0}
          subtitulo={`${resumen?.totalProductosActivos ?? 0} activos en MongoDB`}
          icono={Package}
          color="emerald"
        />
        <StatCard
          titulo="Facturas Emitidas"
          valor={resumen?.totalFacturas ?? 0}
          subtitulo="Transaccional ACID en MySQL"
          icono={Receipt}
          color="blue"
        />
        <StatCard
          titulo="Ventas Acumuladas"
          valor={`$ ${(resumen?.ventasAcumuladas ?? 0).toLocaleString('es-CO', {
            minimumFractionDigits: 2,
          })}`}
          subtitulo="Solo Efectivo (Sin IVA)"
          icono={DollarSign}
          color="purple"
        />
      </div>

      {/* Two columns: Recent Invoices & Top Products (Druid Analytics) */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Recent Invoices (2 cols) */}
        <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-200 shadow-xs p-6">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="text-lg font-bold text-slate-900">Facturas Recientes</h3>
              <p className="text-xs text-slate-500">Últimos registros transaccionales en MySQL</p>
            </div>
            <Link
              to="/facturas"
              className="text-xs font-semibold text-indigo-600 hover:text-indigo-800 flex items-center gap-1"
            >
              Ver todas <ExternalLink className="w-3.5 h-3.5" />
            </Link>
          </div>

          {resumen?.facturasRecientes && resumen.facturasRecientes.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead>
                  <tr className="border-b border-slate-100 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                    <th className="pb-3">N° Factura</th>
                    <th className="pb-3">Cliente</th>
                    <th className="pb-3">Fecha</th>
                    <th className="pb-3 text-right">Total</th>
                    <th className="pb-3 text-center">Acción</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {resumen.facturasRecientes.map((f) => (
                    <tr key={f.idFactura} className="hover:bg-slate-50/60 transition-colors">
                      <td className="py-3.5 font-mono font-medium text-indigo-600">
                        {f.numeroFactura}
                      </td>
                      <td className="py-3.5 text-slate-700 font-medium">
                        {f.clienteNombre}
                      </td>
                      <td className="py-3.5 text-slate-500 text-xs">
                        {new Date(f.fechaFactura).toLocaleString('es-CO')}
                      </td>
                      <td className="py-3.5 text-right font-semibold text-slate-900">
                        ${' '}
                        {f.total.toLocaleString('es-CO', {
                          minimumFractionDigits: 2,
                        })}
                      </td>
                      <td className="py-3.5 text-center">
                        <Link
                          to={`/facturas/${f.idFactura}`}
                          className="px-2.5 py-1 text-xs font-medium text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-md transition-colors"
                        >
                          Ver
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="text-center py-12 text-slate-400 text-sm">
              No hay facturas registradas en MySQL todavía.
            </div>
          )}
        </div>

        {/* Top Products (Druid Analítico) (1 col) */}
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-6 flex flex-col">
          <div className="flex items-center gap-2 mb-2">
            <TrendingUp className="w-5 h-5 text-indigo-600" />
            <h3 className="text-lg font-bold text-slate-900">Más Vendidos</h3>
          </div>
          <p className="text-xs text-slate-500 mb-6">
            Métricas analíticas agregadas en Apache Druid
          </p>

          {resumen?.productosMasVendidos && resumen.productosMasVendidos.length > 0 ? (
            <div className="space-y-4 flex-1">
              {resumen.productosMasVendidos.map((prod, idx) => (
                <div
                  key={prod.idProducto || idx}
                  className="p-3.5 rounded-xl border border-slate-100 bg-slate-50/50 flex items-center justify-between"
                >
                  <div className="min-w-0 pr-3">
                    <p className="font-semibold text-sm text-slate-800 truncate">
                      {prod.nombreProducto}
                    </p>
                    <p className="text-xs text-slate-500 mt-0.5">
                      {prod.categoriaProducto} · {prod.unidadesVendidas} unidades
                    </p>
                  </div>
                  <div className="text-right shrink-0">
                    <p className="text-sm font-bold text-emerald-600">
                      ${' '}
                      {prod.totalVendido.toLocaleString('es-CO', {
                        minimumFractionDigits: 2,
                      })}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center text-center p-6 bg-slate-50 rounded-xl border border-dashed border-slate-200">
              <TrendingUp className="w-8 h-8 text-slate-300 mb-2" />
              <p className="text-xs text-slate-500">
                Los datos analíticos se completarán a medida que se sincronicen los eventos del Outbox hacia Apache Druid.
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
