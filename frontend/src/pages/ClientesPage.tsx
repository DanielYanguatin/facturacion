import React, { useEffect, useState } from 'react';
import {
  Users,
  Search,
  Plus,
  Edit2,
  Eye,
  Database,
  MapPin,
  Mail,
  Phone,
  Tag,
} from 'lucide-react';
import { clienteService } from '../services/clienteService';
import type { Cliente, CrearClientePayload } from '../types';
import { Modal } from '../components/Modal';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { AlertBanner } from '../components/AlertBanner';

export const ClientesPage: React.FC = () => {
  const [clientes, setClientes] = useState<Cliente[]>([]);
  const [cargando, setCargando] = useState(true);
  const [busqueda, setBusqueda] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [mensajeExito, setMensajeExito] = useState<string | null>(null);

  // Modales
  const [modalCrearAbierto, setModalCrearAbierto] = useState(false);
  const [modalEditarAbierto, setModalEditarAbierto] = useState(false);
  const [modalVerAbierto, setModalVerAbierto] = useState(false);
  const [clienteSeleccionado, setClienteSeleccionado] = useState<Cliente | null>(null);

  // Form State
  const [formData, setFormData] = useState<CrearClientePayload>({
    nombreCompleto: '',
    ciudad: '',
    correo: '',
    telefono: '',
    categoria: '',
  });
  const [guardando, setGuardando] = useState(false);

  const cargarClientes = async (filtro?: string) => {
    try {
      setCargando(true);
      setError(null);
      const data = await clienteService.listar(filtro);
      setClientes(data);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Error al consultar clientes en Cassandra.');
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => {
    cargarClientes();
  }, []);

  const handleBuscar = (e: React.FormEvent) => {
    e.preventDefault();
    cargarClientes(busqueda);
  };

  const abrirModalCrear = () => {
    setFormData({
      nombreCompleto: '',
      ciudad: '',
      correo: '',
      telefono: '',
      categoria: '',
    });
    setModalCrearAbierto(true);
  };

  const abrirModalEditar = (cliente: Cliente) => {
    setClienteSeleccionado(cliente);
    setFormData({
      nombreCompleto: cliente.nombreCompleto,
      ciudad: cliente.ciudad,
      correo: cliente.correo,
      telefono: cliente.telefono,
      categoria: cliente.categoria || '',
    });
    setModalEditarAbierto(true);
  };

  const abrirModalVer = (cliente: Cliente) => {
    setClienteSeleccionado(cliente);
    setModalVerAbierto(true);
  };

  const handleSubmitCrear = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setGuardando(true);
      setError(null);
      await clienteService.crear(formData);
      setMensajeExito('Cliente registrado con éxito en Apache Cassandra.');
      setModalCrearAbierto(false);
      cargarClientes();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Error al registrar cliente.');
    } finally {
      setGuardando(false);
    }
  };

  const handleSubmitEditar = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!clienteSeleccionado) return;
    try {
      setGuardando(true);
      setError(null);
      await clienteService.actualizar(clienteSeleccionado.idCliente, formData);
      setMensajeExito('Cliente actualizado con éxito en Apache Cassandra.');
      setModalEditarAbierto(false);
      cargarClientes();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Error al actualizar cliente.');
    } finally {
      setGuardando(false);
    }
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
              Gestión de Clientes
            </h1>
            <span className="px-2 py-0.5 bg-emerald-100 text-emerald-800 text-[11px] font-bold rounded flex items-center gap-1">
              <Database className="w-3 h-3" /> Cassandra
            </span>
          </div>
          <p className="text-sm text-slate-500 mt-1">
            Persistencia NoSQL en clavespace <code>facturacion_clientes</code> (tablas <code>clientes_por_id</code> y <code>clientes_por_correo</code>).
          </p>
        </div>
        <button
          onClick={abrirModalCrear}
          className="inline-flex items-center gap-2 px-5 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-sm font-semibold shadow-sm transition-all cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          Registrar Cliente
        </button>
      </div>

      {error && <AlertBanner tipo="error" mensaje={error} onCerrar={() => setError(null)} />}
      {mensajeExito && <AlertBanner tipo="exito" mensaje={mensajeExito} onCerrar={() => setMensajeExito(null)} />}

      {/* Search & Filters */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex items-center gap-4">
        <form onSubmit={handleBuscar} className="flex-1 flex items-center gap-2">
          <div className="relative flex-1">
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
            <input
              type="text"
              placeholder="Buscar por nombre, correo, teléfono, ciudad o ID..."
              value={busqueda}
              onChange={(e) => setBusqueda(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
          <button
            type="submit"
            className="px-4 py-2 bg-slate-800 hover:bg-slate-900 text-white text-sm font-medium rounded-lg transition-colors cursor-pointer"
          >
            Buscar
          </button>
          {busqueda && (
            <button
              type="button"
              onClick={() => {
                setBusqueda('');
                cargarClientes('');
              }}
              className="px-3 py-2 text-slate-500 hover:text-slate-800 text-xs font-semibold cursor-pointer"
            >
              Limpiar
            </button>
          )}
        </form>
      </div>

      {/* Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
        {cargando ? (
          <LoadingSpinner texto="Consultando clientes en Cassandra..." />
        ) : clientes.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                <tr>
                  <th className="px-6 py-3.5">Cliente</th>
                  <th className="px-6 py-3.5">Ubicación & Contacto</th>
                  <th className="px-6 py-3.5">Categoría</th>
                  <th className="px-6 py-3.5">Fecha Registro</th>
                  <th className="px-6 py-3.5 text-right">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {clientes.map((c) => (
                  <tr key={c.idCliente} className="hover:bg-slate-50/70 transition-colors">
                    <td className="px-6 py-4">
                      <div className="font-semibold text-slate-900">{c.nombreCompleto}</div>
                      <div className="text-xs text-slate-400 font-mono mt-0.5 truncate max-w-xs">
                        ID: {c.idCliente}
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-1.5 text-xs text-slate-600">
                        <MapPin className="w-3.5 h-3.5 text-slate-400" /> {c.ciudad}
                      </div>
                      <div className="flex items-center gap-1.5 text-xs text-slate-500 mt-1">
                        <Mail className="w-3.5 h-3.5 text-slate-400" /> {c.correo}
                      </div>
                      <div className="flex items-center gap-1.5 text-xs text-slate-500 mt-0.5">
                        <Phone className="w-3.5 h-3.5 text-slate-400" /> {c.telefono}
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      {c.categoria ? (
                        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-indigo-50 text-indigo-700 border border-indigo-100">
                          <Tag className="w-3 h-3" /> {c.categoria}
                        </span>
                      ) : (
                        <span className="text-xs text-slate-400 italic">Sin categoría</span>
                      )}
                    </td>
                    <td className="px-6 py-4 text-xs text-slate-500">
                      {new Date(c.fechaRegistro).toLocaleDateString('es-CO')}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="inline-flex items-center gap-1">
                        <button
                          onClick={() => abrirModalVer(c)}
                          title="Ver detalle"
                          className="p-1.5 text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-lg transition-colors cursor-pointer"
                        >
                          <Eye className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => abrirModalEditar(c)}
                          title="Editar"
                          className="p-1.5 text-indigo-600 hover:text-indigo-800 hover:bg-indigo-50 rounded-lg transition-colors cursor-pointer"
                        >
                          <Edit2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="text-center py-16 text-slate-400 space-y-2">
            <Users className="w-12 h-12 mx-auto text-slate-300" />
            <p className="font-semibold text-slate-600">No hay clientes registrados</p>
            <p className="text-xs">Comienza registrando tu primer cliente en Cassandra.</p>
          </div>
        )}
      </div>

      {/* Modal Registrar Cliente */}
      <Modal
        isOpen={modalCrearAbierto}
        onClose={() => setModalCrearAbierto(false)}
        titulo="Registrar Nuevo Cliente en Cassandra"
      >
        <form onSubmit={handleSubmitCrear} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
              Nombre Completo *
            </label>
            <input
              type="text"
              required
              value={formData.nombreCompleto}
              onChange={(e) => setFormData({ ...formData, nombreCompleto: e.target.value })}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
              placeholder="Ej. Juan Pérez"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                Ciudad *
              </label>
              <input
                type="text"
                required
                value={formData.ciudad}
                onChange={(e) => setFormData({ ...formData, ciudad: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
                placeholder="Ej. Bogotá"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                Teléfono *
              </label>
              <input
                type="text"
                required
                value={formData.telefono}
                onChange={(e) => setFormData({ ...formData, telefono: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
                placeholder="Ej. 3001234567"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
              Correo Electrónico *
            </label>
            <input
              type="email"
              required
              value={formData.correo}
              onChange={(e) => setFormData({ ...formData, correo: e.target.value })}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
              placeholder="juan.perez@example.com"
            />
            <p className="text-[11px] text-slate-400 mt-1">
              El correo se indexa en <code>clientes_por_correo</code> para validar unicidad.
            </p>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
              Categoría (Opcional)
            </label>
            <input
              type="text"
              value={formData.categoria}
              onChange={(e) => setFormData({ ...formData, categoria: e.target.value })}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
              placeholder="Ej. Regular, Frecuente, Corporativo..."
            />
          </div>

          <div className="pt-4 border-t border-slate-100 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={() => setModalCrearAbierto(false)}
              className="px-4 py-2 border border-slate-300 text-slate-700 hover:bg-slate-50 rounded-lg text-sm font-medium cursor-pointer"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={guardando}
              className="px-5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-semibold shadow-xs disabled:opacity-50 cursor-pointer"
            >
              {guardando ? 'Guardando en Cassandra...' : 'Guardar Cliente'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Modal Editar Cliente */}
      <Modal
        isOpen={modalEditarAbierto}
        onClose={() => setModalEditarAbierto(false)}
        titulo="Editar Cliente en Cassandra"
      >
        <form onSubmit={handleSubmitEditar} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
              Nombre Completo *
            </label>
            <input
              type="text"
              required
              value={formData.nombreCompleto}
              onChange={(e) => setFormData({ ...formData, nombreCompleto: e.target.value })}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                Ciudad *
              </label>
              <input
                type="text"
                required
                value={formData.ciudad}
                onChange={(e) => setFormData({ ...formData, ciudad: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                Teléfono *
              </label>
              <input
                type="text"
                required
                value={formData.telefono}
                onChange={(e) => setFormData({ ...formData, telefono: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
              Correo Electrónico *
            </label>
            <input
              type="email"
              required
              value={formData.correo}
              onChange={(e) => setFormData({ ...formData, correo: e.target.value })}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
              Categoría
            </label>
            <input
              type="text"
              value={formData.categoria}
              onChange={(e) => setFormData({ ...formData, categoria: e.target.value })}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div className="pt-4 border-t border-slate-100 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={() => setModalEditarAbierto(false)}
              className="px-4 py-2 border border-slate-300 text-slate-700 hover:bg-slate-50 rounded-lg text-sm font-medium cursor-pointer"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={guardando}
              className="px-5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-semibold shadow-xs disabled:opacity-50 cursor-pointer"
            >
              {guardando ? 'Actualizando...' : 'Actualizar Cliente'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Modal Ver Detalle */}
      <Modal
        isOpen={modalVerAbierto}
        onClose={() => setModalVerAbierto(false)}
        titulo="Detalle del Cliente (Cassandra)"
      >
        {clienteSeleccionado && (
          <div className="space-y-4">
            <div className="p-4 bg-slate-50 rounded-xl space-y-2 text-sm">
              <div>
                <span className="text-xs font-bold text-slate-400 uppercase">UUID del Cliente</span>
                <p className="font-mono text-xs font-semibold text-indigo-700 select-all">
                  {clienteSeleccionado.idCliente}
                </p>
              </div>
              <div>
                <span className="text-xs font-bold text-slate-400 uppercase">Nombre Completo</span>
                <p className="font-semibold text-slate-800">{clienteSeleccionado.nombreCompleto}</p>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <span className="text-xs font-bold text-slate-400 uppercase">Ciudad</span>
                  <p className="text-slate-700">{clienteSeleccionado.ciudad}</p>
                </div>
                <div>
                  <span className="text-xs font-bold text-slate-400 uppercase">Teléfono</span>
                  <p className="text-slate-700">{clienteSeleccionado.telefono}</p>
                </div>
              </div>
              <div>
                <span className="text-xs font-bold text-slate-400 uppercase">Correo Electrónico</span>
                <p className="text-slate-700">{clienteSeleccionado.correo}</p>
              </div>
              <div>
                <span className="text-xs font-bold text-slate-400 uppercase">Categoría</span>
                <p className="text-slate-700">{clienteSeleccionado.categoria || 'Sin categoría'}</p>
              </div>
              <div className="grid grid-cols-2 gap-4 pt-2 border-t border-slate-200 text-xs text-slate-500">
                <div>
                  <span className="font-semibold">Registrado:</span>{' '}
                  {new Date(clienteSeleccionado.fechaRegistro).toLocaleString('es-CO')}
                </div>
                <div>
                  <span className="font-semibold">Actualizado:</span>{' '}
                  {new Date(clienteSeleccionado.actualizadoEn).toLocaleString('es-CO')}
                </div>
              </div>
            </div>
            <div className="flex justify-end">
              <button
                onClick={() => setModalVerAbierto(false)}
                className="px-4 py-2 bg-slate-800 text-white rounded-lg text-sm font-medium cursor-pointer"
              >
                Cerrar
              </button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};
