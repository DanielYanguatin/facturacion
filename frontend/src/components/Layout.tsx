import React from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import {
  LayoutDashboard,
  Users,
  Package,
  Receipt,
  FileText,
  Database,
  Layers,
} from 'lucide-react';

export const Layout: React.FC = () => {
  const navItems = [
    { to: '/', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/clientes', label: 'Clientes (Cassandra)', icon: Users },
    { to: '/productos', label: 'Productos (MongoDB)', icon: Package },
    { to: '/facturas/nueva', label: 'Nueva Factura', icon: Receipt },
    { to: '/facturas', label: 'Historial Facturas (MySQL)', icon: FileText },
  ];

  return (
    <div className="min-h-screen flex bg-slate-100">
      {/* Sidebar */}
      <aside className="w-64 bg-slate-900 text-slate-100 flex flex-col no-print shrink-0 shadow-xl">
        <div className="p-6 border-b border-slate-800">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-indigo-600 rounded-lg text-white">
              <Layers className="w-6 h-6" />
            </div>
            <div>
              <h1 className="font-bold text-lg tracking-tight">FacturaHexa</h1>
              <p className="text-xs text-slate-400">Multi-DB NoSQL & SQL</p>
            </div>
          </div>
        </div>

        <nav className="flex-1 p-4 space-y-1.5">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === '/'}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-indigo-600 text-white shadow-sm'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                  }`
                }
              >
                <Icon className="w-5 h-5" />
                <span>{item.label}</span>
              </NavLink>
            );
          })}
        </nav>

        {/* Database badges */}
        <div className="p-4 border-t border-slate-800 text-xs space-y-2">
          <p className="font-semibold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
            <Database className="w-3.5 h-3.5" /> Motores Activos
          </p>
          <div className="grid grid-cols-2 gap-1.5 font-mono text-[11px]">
            <span className="px-2 py-1 bg-slate-800 text-emerald-400 rounded flex items-center gap-1">
              <span className="w-1.5 h-1.5 bg-emerald-400 rounded-full"></span> Cassandra
            </span>
            <span className="px-2 py-1 bg-slate-800 text-emerald-400 rounded flex items-center gap-1">
              <span className="w-1.5 h-1.5 bg-emerald-400 rounded-full"></span> MongoDB
            </span>
            <span className="px-2 py-1 bg-slate-800 text-emerald-400 rounded flex items-center gap-1">
              <span className="w-1.5 h-1.5 bg-emerald-400 rounded-full"></span> MySQL
            </span>
            <span className="px-2 py-1 bg-slate-800 text-emerald-400 rounded flex items-center gap-1">
              <span className="w-1.5 h-1.5 bg-emerald-400 rounded-full"></span> Druid
            </span>
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <div className="flex-1 flex flex-col min-w-0 overflow-y-auto">
        <header className="bg-white border-b border-slate-200 px-8 py-4 no-print flex items-center justify-between sticky top-0 z-10 shadow-sm">
          <div>
            <span className="text-xs uppercase tracking-wider font-semibold text-indigo-600">
              Arquitectura Hexagonal
            </span>
            <h2 className="text-xl font-bold text-slate-800">
              Sistema de Facturación Académico
            </h2>
          </div>
          <div className="flex items-center gap-4 text-xs text-slate-500">
            <span className="px-2.5 py-1 bg-indigo-50 text-indigo-700 font-medium rounded-full">
              Spring Boot 3 + Java 21
            </span>
            <span className="px-2.5 py-1 bg-slate-100 text-slate-700 font-medium rounded-full">
              Solo Efectivo (Sin IVA/Desc)
            </span>
          </div>
        </header>

        <main className="flex-1 p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
